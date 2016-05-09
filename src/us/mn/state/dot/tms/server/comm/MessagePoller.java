/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.server.comm;

import java.io.IOException;
import java.net.SocketTimeoutException;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * MessagePoller is an abstract class which represents a communication channel 
 * with priority-queued polling.  Subclasses are MndotPoller, NtcipPoller, etc.
 *
 * @author Douglas Lau
 */
abstract public class MessagePoller<T extends ControllerProperty>
	implements DevicePoller
{
	/** Get a message describing an IO exception */
	static protected String exceptionMessage(IOException e) {
		String m = e.getMessage();
		if (m != null && m.length() > 0)
			return m;
		else
			return e.getClass().getSimpleName();
	}

	/** Message polling log */
	static private final DebugLog POLL_LOG = new DebugLog("polling");

	/** Priority change log */
	static private final DebugLog PRIO_LOG = new DebugLog("prio");

	/** Thread group for all message poller threads */
	static private final ThreadGroup GROUP = new ThreadGroup("Poller");

	/** Thread state */
	static private enum ThreadState {
		NOT_STARTED,
		STARTING,
		RUNNING,
		CLOSING,
		STOPPED;
	}

	/** Write a message to the polling log */
	private void plog(String msg) {
		if (POLL_LOG.isOpen())
			POLL_LOG.log(thread.getName() + " " + msg);
	}

	/** Thread to poll operations */
	private final Thread thread;

	/** Operation queue */
	protected final OpQueue<T> queue = new OpQueue<T>();

	/** Messenger for poll/response streams */
	protected final Messenger messenger;

	/** Thread state */
	private ThreadState state = ThreadState.NOT_STARTED;

	/** Set the thread state */
	private synchronized void setThreadState(ThreadState st) {
		state = st;
		plog("state: " + st);
	}

	/** Poller status */
	private String status = null;

	/** Set the poller status */
	protected void setStatus(String s) {
		status = s;
	}

	/** Get the poller status */
	@Override
	public String getStatus() {
		String s = status;
		if (s != null)
			return s;
		ThreadState ts = state;
		return (ts == ThreadState.RUNNING) ? "" : ts.toString();
	}

	/** Check if ready for operation */
	@Override
	public synchronized boolean isReady() {
		switch (state) {
		case NOT_STARTED:
		case STARTING:
		case RUNNING:
			return true;
		default:
			return false;
		}
	}

	/** Check if poller is connected */
	@Override
	public synchronized boolean isConnected() {
		switch (state) {
		case STARTING:
		case RUNNING:
			return true;
		default:
			return false;
		}
	}

	/** Hung up flag */
	private boolean hung_up = false;

	/** Check if the messenger was hung up on */
	@Override
	public final boolean wasHungUp() {
		return hung_up;
	}

	/** Create a new message poller */
	protected MessagePoller(String name, Messenger m) {
 		thread = new Thread(GROUP, "Poller: " + name) {
			@Override
			public void run() {
				operationLoop();
			}
		};
		thread.setDaemon(true);
		setThreadState(ThreadState.NOT_STARTED);
		messenger = m;
	}

	/** Set the receive timeout */
	@Override
	public final void setTimeout(int t) throws IOException {
		messenger.setTimeout(t);
	}

	/** Add an operation to the message poller */
	protected void addOp(OpController<T> op) {
		if (queue.enqueue(op))
			ensureStarted();
		else
			plog("DROPPING " + op);
	}

	/** Ensure the thread is started */
	private void ensureStarted() {
		if (shouldStart())
			startPolling();
	}

	/** Should the thread be started? */
	private synchronized boolean shouldStart() {
		if (state == ThreadState.NOT_STARTED) {
			setThreadState(ThreadState.STARTING);
			return true;
		} else
			return false;
	}

	/** Start polling */
	protected void startPolling() {
		thread.start();
	}

	/** Stop polling */
	protected void stopPolling() {
		queue.close();
		thread.interrupt();
	}

	/** Destroy the poller */
	@Override
	public final void destroy() {
		if (isConnected())
			stopPolling();
	}

	/** Open messenger and perform operations */
	private void operationLoop() {
		try {
			messenger.open();
			setThreadState(ThreadState.RUNNING);
			performOperations();
			setThreadState(ThreadState.CLOSING);
		}
		catch (HangUpException e) {
			setStatus(exceptionMessage(e));
			hung_up = true;
		}
		catch (IOException e) {
			setStatus(exceptionMessage(e));
		}
		catch (InterruptedException e) {
			// from stopPolling
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
		finally {
			messenger.close();
			drainQueue();
			setThreadState(ThreadState.STOPPED);
		}
	}

	/** Drain the poll queue */
	private void drainQueue() {
		final String s = getStatus();
		queue.forEach(new OpHandler<T>() {
			public void handle(PriorityLevel prio,
				OpController<T> o)
			{
				o.handleCommError(EventType.QUEUE_DRAINED, s);
				if (hung_up)
					o.setSucceeded();
				o.cleanup();
			}
		});
	}

	/** Perform operations on the poll queue */
	private void performOperations() throws IOException,
		InterruptedException
	{
		while (queue.isOpen())
			doPoll(queue.next());
	}

	/** Perform one poll for an operation */
	private void doPoll(final OpController<T> o) throws IOException {
		final String oname = o.toString();
		long start = TimeSteward.currentTimeMillis();
		try {
			o.poll(createCommMessage(o));
		}
		catch (DeviceContentionException e) {
			handleContention(o, e);
		}
		catch (DownloadRequestException e) {
			download(o.getController(), o.getPriority());
		}
		catch (ChecksumException e) {
			o.handleCommError(EventType.CHECKSUM_ERROR,
				exceptionMessage(e));
			messenger.drain();
		}
		catch (ParsingException e) {
			o.handleCommError(EventType.PARSING_ERROR,
				exceptionMessage(e));
			messenger.drain();
		}
		catch (ControllerException e) {
			o.handleCommError(EventType.CONTROLLER_ERROR,
				exceptionMessage(e));
			o.setFailed();
			o.setMaintStatus(exceptionMessage(e));
		}
		catch (SocketTimeoutException e) {
			o.handleCommError(EventType.POLL_TIMEOUT_ERROR,
				exceptionMessage(e));
		}
		finally {
			if (o.isDone() || !requeueOperation(o))
				o.cleanup();
			if (POLL_LOG.isOpen()) {
				plog(oname + " elapsed: " +
					calculate_elapsed(start));
			}
		}
	}

	/** Handle device contention.  Another operation has the device lock.
	 * Ensure that we don't have a priority inversion problem. */
	@SuppressWarnings("unchecked")
	private void handleContention(OpController<T> op,
		DeviceContentionException e)
	{
		handleContention(op, e.operation);
	}

	/** Handle device contention */
	private void handleContention(OpController<T> op, OpController<T> oc) {
		if (oc.getPriority().ordinal() > op.getPriority().ordinal()) {
			if (PRIO_LOG.isOpen()) {
				PRIO_LOG.log("BUMPING " + oc + " from " +
					oc.getPriority() + " to " +
					op.getPriority());
			}
			oc.setPriority(op.getPriority());
			// If, for some crazy reason, the operation is
			// not on our queue, it will not be requeued.
			if (!requeueOperation(oc)) {
				oc.setFailed();
				oc.cleanup();
			}
		}
	}

	/** Requeue an in-progress operation */
	private boolean requeueOperation(OpController<T> op) {
		if (queue.requeue(op))
			return true;
		else {
			plog("DROPPING " + op);
			return false;
		}
	}

	/** Calculate the elapsed time */
	private long calculate_elapsed(long start) {
		return TimeSteward.currentTimeMillis() - start;
	}

	/** Create a message for the specified operation.
	 * @param o The operation.
	 * @return New comm message. */
	protected CommMessage<T> createCommMessage(OpController<T> o)
		throws IOException
	{
		return new CommMessageImpl<T>(messenger, o, protocolLog());
	}

	/** Get the protocol debug log */
	protected DebugLog protocolLog() {
		return null;
	}

	/** Respond to a download request from a controller */
	protected void download(ControllerImpl c, PriorityLevel p) {
		// Subclasses should override this if necessary
	}
}
