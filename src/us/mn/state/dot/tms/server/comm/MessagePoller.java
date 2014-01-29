/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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
	extends Thread
{
	/** Create a message poller */
	static public MessagePoller create(String name, CommProtocol protocol,
		String uri) throws IOException
	{
		MessagePollerFactory factory = new MessagePollerFactory(name,
			protocol, uri);
		return factory.create();
	}

	/** Get a message describing an IO exception */
	static protected String exceptionMessage(IOException e) {
		String m = e.getMessage();
		if(m != null && m.length() > 0)
			return m;
		else
			return e.getClass().getSimpleName();
	}

	/** Message polling log */
	static private final DebugLog POLL_LOG = new DebugLog("polling");

	/** Thread group for all message poller threads */
	static private final ThreadGroup GROUP = new ThreadGroup("Poller");

	/** Write a message to the polling log */
	protected void plog(String msg) {
		if(POLL_LOG.isOpen())
			POLL_LOG.log(getName() + " " + msg);
	}

	/** Operation queue */
	protected final OperationQueue<T> queue = new OperationQueue<T>();

	/** Messenger for poll/response streams */
	protected final Messenger messenger;

	/** Poller status (null means not initialized yet) */
	protected String status = null;

	/** Get the poller status */
	public String getStatus() {
		return status;
	}

	/** Hung up flag */
	private boolean hung_up = false;

	/** Check if the messenger was hung up on */
	public boolean wasHungUp() {
		return hung_up;
	}

	/** Create a new message poller */
	protected MessagePoller(String name, Messenger m) {
		super(GROUP, "Poller: " + name);
		setDaemon(true);
		messenger = m;
	}

	/** Set the receive timeout */
	public void setTimeout(int t) throws IOException {
		messenger.setTimeout(t);
	}

	/** Add an operation to the message poller */
	protected void addOperation(Operation<T> op) {
		if(!queue.enqueue(op))
			plog("DROPPING " + op);
	}

	/** Stop polling on this thread */
	public void stopPolling() {
		addOperation(new KillThread<T>());
	}

	/** MessagePoller is a subclass of Thread.  This is the run method. */
	public void run() {
		status = "STARTING";
		plog("STARTING");
		try {
			messenger.open();
			status = "";
			performOperations();
			status = "CLOSING";
		}
		catch(HangUpException e) {
			status = exceptionMessage(e);
			hung_up = true;
		}
		catch(IOException e) {
			status = exceptionMessage(e);
		}
		catch(RuntimeException e) {
			e.printStackTrace();
		}
		finally {
			messenger.close();
			drainQueue();
			plog("STOPPING");
		}
	}

	/** Drain the poll queue */
	private void drainQueue() {
		queue.close();
		while(queue.hasNext()) {
			Operation<T> o = queue.next();
			o.handleCommError(EventType.QUEUE_DRAINED, status);
			if(hung_up)
				o.setSucceeded();
			o.cleanup();
		}
	}

	/** Perform operations on the poll queue */
	private void performOperations() throws IOException {
		while(true) {
			Operation<T> o = queue.next();
			if(o instanceof KillThread)
				break;
			if(o instanceof OpController)
				doPoll((OpController<T>)o);
		}
	}

	/** Perform one poll for an operation */
	private void doPoll(final OpController<T> o) throws IOException {
		final String oname = o.toString();
		long start = TimeSteward.currentTimeMillis();
		try {
			o.poll(createMessage(o.getController()));
		}
		catch(DeviceContentionException e) {
			handleContention(o, e);
		}
		catch(DownloadRequestException e) {
			download(o.getController(), o.getPriority());
		}
		catch(ChecksumException e) {
			o.handleCommError(EventType.CHECKSUM_ERROR,
				exceptionMessage(e));
			messenger.drain();
		}
		catch(ParsingException e) {
			o.handleCommError(EventType.PARSING_ERROR,
				exceptionMessage(e));
			messenger.drain();
		}
		catch(ControllerException e) {
			o.handleCommError(EventType.CONTROLLER_ERROR,
				exceptionMessage(e));
			o.setFailed();
			o.setMaintStatus(exceptionMessage(e));
		}
		catch(SocketTimeoutException e) {
			o.handleCommError(EventType.POLL_TIMEOUT_ERROR,
				exceptionMessage(e));
		}
		finally {
			if(o.isDone() || !requeueOperation(o))
				o.cleanup();
			if(POLL_LOG.isOpen()) {
				plog(oname + " elapsed: " +
					calculate_elapsed(start));
			}
		}
	}

	/** Handle device contention.  Another operation has the device lock.
	 * Ensure that we don't have a priority inversion problem. */
	private void handleContention(Operation<T> op,
		DeviceContentionException e)
	{
		Operation<T> oc = e.operation;
		if(oc.getPriority().ordinal() > op.getPriority().ordinal()) {
			oc.setPriority(op.getPriority());
			// If, for some crazy reason, the operation is
			// not on our queue, it will not be requeued.
			if(!requeueOperation(oc)) {
				oc.setFailed();
				oc.cleanup();
			}
		}
	}

	/** Requeue an in-progress operation */
	private boolean requeueOperation(Operation<T> op) {
		if(queue.requeue(op))
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

	/** Check if a drop address is valid */
	abstract public boolean isAddressValid(int drop);

	/** Create a message for the specified controller */
	protected CommMessage<T> createMessage(ControllerImpl c)
		throws IOException
	{
		return new CommMessageImpl<T>(messenger, c);
	}

	/** Respond to a download request from a controller */
	protected void download(ControllerImpl c, PriorityLevel p) {
		// Subclasses should override this if necessary
	}
}
