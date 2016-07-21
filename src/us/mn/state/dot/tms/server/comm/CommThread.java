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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * CommThread represents a communication channel with priority-queued polling.
 *
 * @author Douglas Lau
 */
public class CommThread<T extends ControllerProperty> {

	/** Get a message describing an exception */
	static private String exceptionMessage(Exception e) {
		String m = e.getMessage();
		if (m != null && m.length() > 0)
			return m;
		else
			return e.getClass().getSimpleName();
	}

	/** Thread group for all comm threads */
	static private final ThreadGroup GROUP = new ThreadGroup("Comm");

	/** Comm error log */
	static private final DebugLog COMM_LOG = new DebugLog("comm");

	/** Write a message to the comm log */
	private void clog(String msg) {
		if (COMM_LOG.isOpen())
			COMM_LOG.log(thread.getName() + " " + msg);
	}

	/** Device poller */
	protected final DevicePoller<T> poller;

	/** Thread to poll operations */
	private final Thread thread;

	/** Operation queue */
	protected final OpQueue<T> queue;

	/** Messenger for poll/response streams */
	protected final Messenger messenger;

	/** Thread status */
	private String status = "STARTING";

	/** Set the thread status */
	protected void setStatus(String s) {
		status = s;
	}

	/** Get the thread status */
	public String getStatus() {
		return status;
	}

	/** Create a new comm thread */
	public CommThread(DevicePoller<T> dp, OpQueue<T> q, Messenger m) {
		poller = dp;
 		thread = new Thread(GROUP, "Comm: " + poller.name) {
			@Override
			public void run() {
				operationLoop();
			}
		};
		thread.setDaemon(true);
		queue = q;
		messenger = m;
	}

	/** Start the thread */
	public void start() {
		thread.start();
	}

	/** Check if the thread is alive */
	public boolean isAlive() {
		return thread.isAlive();
	}

	/** Destroy the comm thread */
	public void destroy() {
		thread.interrupt();
	}

	/** Open messenger and perform operations */
	private void operationLoop() {
		try {
			messenger.open();
			performOperations();
		}
		catch (InterruptedException e) {
			setStatus(getMessage(e));
		}
		catch (IOException e) {
			setStatus(getMessage(e));
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
		finally {
			messenger.close();
		}
	}

	/** Get an exception message */
	protected String getMessage(Exception e) {
		String msg = exceptionMessage(e);
		clog(msg);
		return msg;
	}

	/** Perform operations on the poll queue */
	private void performOperations() throws IOException,
		InterruptedException
	{
		while (queue.isOpen()) {
			doPoll(queue.next());
			setStatus("");
		}
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
			sendSettings(o.getController(), o.getPriority());
		}
		catch (ProtocolException e) {
			String msg = getMessage(e);
			o.setFailed();
			o.setMaintStatus(msg);
		}
		catch (ChecksumException e) {
			String msg = getMessage(e);
			o.handleCommError(EventType.CHECKSUM_ERROR, msg);
			messenger.drain();
		}
		catch (ParsingException e) {
			String msg = getMessage(e);
			o.handleCommError(EventType.PARSING_ERROR, msg);
			messenger.drain();
		}
		catch (ControllerException e) {
			String msg = getMessage(e);
			o.handleCommError(EventType.CONTROLLER_ERROR, msg);
			o.setFailed();
			o.setMaintStatus(msg);
		}
		catch (SocketTimeoutException e) {
			String msg = getMessage(e);
			o.handleCommError(EventType.POLL_TIMEOUT_ERROR, msg);
		}
		catch (SocketException e) {
			String msg = getMessage(e);
			o.handleCommError(EventType.COMM_ERROR, msg);
			messenger.close();
			messenger.open();
		}
		finally {
			if (o.isDone() || !requeueOperation(o))
				o.cleanup();
			if (COMM_LOG.isOpen()) {
				clog(oname + " elapsed: " +
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
			poller.log("BUMPING " + oc + " from " + oc.getPriority()
				+ " to " + op.getPriority());
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
			poller.log("DROPPING " + op);
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
		return new CommMessageImpl<T>(messenger, o, poller.logger);
	}

	/** Respond to a settings request from a controller */
	private void sendSettings(ControllerImpl c, PriorityLevel p) {
		if (c.isActive())
			poller.sendSettings(c, p);
	}
}
