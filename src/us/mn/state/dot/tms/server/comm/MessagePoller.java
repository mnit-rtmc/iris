/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DebugLog;
import us.mn.state.dot.tms.server.event.EventType;

/**
 * MessagePoller is an abstract class which represents a communication channel 
 * with priority-queued polling.  Subclasses are MndotPoller, NtcipPoller, etc.
 *
 * @author Douglas Lau
 */
abstract public class MessagePoller extends Thread {

	/** Get a message describing an IO exception */
	static protected String exceptionMessage(IOException e) {
		String m = e.getMessage();
		if(m != null && m.length() > 0)
			return m;
		else
			return e.getClass().getSimpleName();
	}

	/** Interval time for load calculation is 30,000 miliseconds */
	static protected final long INTERVAL_TIME = 30000;

	/** Message polling log */
	static protected final DebugLog POLL_LOG = new DebugLog("polling");

	/** Thread group for all message poller threads */
	static protected final ThreadGroup GROUP = new ThreadGroup("Poller");

	/** Message Poll queue */
	protected final PollQueue queue = new PollQueue();

	/** Messenger for poll/response streams */
	protected final Messenger messenger;

	/** Print the current status of the message poller */
	public void print(PrintStream ps) {
		queue.print(ps);
	}

	/** Interval start time of current load calculation */
	protected long interval = 0;

	/** Busy time of curent load calculation */
	protected long busy = 0;

	/** Load average (for last completed interval) */
	protected float load = 0;

	/** Poller status (null means not initialized yet) */
	protected String status = null;

	/** Get the poller status */
	public String getStatus() {
		return status;
	}

	/** Create a new message poller */
	public MessagePoller(String name, Messenger m) {
		super(GROUP, "Poller: " + name);
		setDaemon(true);
		messenger = m;
	}

	/** Set the receive timeout */
	public void setTimeout(int t) throws IOException {
		messenger.setTimeout(t);
	}

	/** Add an operation to the message poller */
	public void addOperation(Operation o) {
		// NOTE: we must synchronize on the queue here so that begin()
		// gets called before the poll queue thread get access to it
		synchronized(queue) {
			if(queueOperation(o))
				o.begin();
		}
	}

	/** Queue an operation to be processed */
	protected boolean queueOperation(Operation o) {
		if(queue.add(o))
			return true;
		else {
			if(POLL_LOG.isOpen())
				POLL_LOG.log(getName() + ", DROPPING: " + o);
			return false;
		}
	}

	/** Stop polling on this thread */
	public void stopPolling() {
		addOperation(new KillThread());
		try {
			join();
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	/** MessagePoller is a subclass of Thread.  This is the run method. */
	public void run() {
		if(POLL_LOG.isOpen())
			POLL_LOG.log(getName() + " STARTING");
		try {
			messenger.open();
			status = "";
			performOperations();
			status = "CLOSING";
		}
		catch(IOException e) {
			status = exceptionMessage(e);
		}
		finally {
			messenger.close();
			drainQueue();
			if(POLL_LOG.isOpen())
				POLL_LOG.log(getName() + " STOPPING");
		}
	}

	/** Drain the poll queue */
	protected void drainQueue() {
		queue.close();
		while(queue.hasNext()) {
			Operation o = queue.next();
			o.handleCommError(EventType.QUEUE_DRAINED, status);
			o.cleanup();
		}
	}

	/** Perform operations on the poll queue */
	protected void performOperations() throws IOException {
		while(true) {
			Operation o = queue.next();
			if(o instanceof KillThread)
				break;
			if(o instanceof OpController)
				doPoll((OpController)o);
		}
	}

	/** Perform one poll for an operation */
	protected void doPoll(final OpController o) throws IOException {
		final String oname = o.toString();
		long start = System.currentTimeMillis();
		try {
			o.poll(createMessage(o.getController()));
		}
		catch(DeviceContentionException e) {
			// Another operation has the device lock. Raise its
			// priority to that of the current operation.
			Operation oc = e.operation;
			if(oc.getPriority() > o.getPriority()) {
				queue.remove(oc);
				oc.setPriority(o.getPriority());
				if(!queueOperation(oc))
					oc.cleanup();
			}
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
			o.setErrorStatus(exceptionMessage(e));
		}
		catch(SocketTimeoutException e) {
			o.handleCommError(EventType.POLL_TIMEOUT_ERROR,
				exceptionMessage(e));
		}
		catch(NumberFormatException e) {
			o.handleCommError(EventType.PARSING_ERROR,
				"NUMBER FORMAT ERROR");
		}
		finally {
			if(o.isDone() || !queueOperation(o))
				o.cleanup();
			if(POLL_LOG.isOpen()) {
				long el = sample_load(start);
				POLL_LOG.log(getName() + ", " + oname +
					" elapsed: " + el);
			}
		}
	}

	/** Sample the current line load */
	protected long sample_load(long start) {
		long now = System.currentTimeMillis();
		long e = now - start;
		long past = now % INTERVAL_TIME;
		long i = now - past;
		if(i == interval)
			busy += e;
		else if(i > interval) {
			if(e > past)
				busy += e - past;
			load = (float)busy / (i - interval);
			if(e > past)
				busy = past;
			else
				busy = e;
		}
		interval = i;
		return e;
	}

	/** Get the current line usage load */
	public float getLoad() {
		return load;
	}

	/** Check if a drop address is valid */
	abstract public boolean isAddressValid(int drop);

	/** Create a message for the specified controller */
	abstract protected AddressedMessage createMessage(ControllerImpl c)
		throws EOFException;

	/** Respond to a download request from a controller */
	protected void download(ControllerImpl c, int p) {
		// Subclasses should override this if necessary
	}
}
