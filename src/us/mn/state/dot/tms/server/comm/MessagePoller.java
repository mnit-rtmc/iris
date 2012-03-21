/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
 * Copyright (C) 2012  Iteris Inc.
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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.IDebugLog;

/**
 * MessagePoller is an abstract class which represents a communication channel 
 * with priority-queued polling.  Subclasses are MndotPoller, NtcipPoller, etc.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
abstract public class MessagePoller extends Thread {

	/** Messenger open modes */
	public enum OpenMode {PERSISTENT, PER_OP};

	/** Messenger open mode */
	protected final OpenMode open_mode;

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

	/** Interval time for load calculation is 30,000 miliseconds */
	static protected final long INTERVAL_TIME = 30000;

	/** Message polling log */
	static protected final IDebugLog POLL_LOG = new IDebugLog("polling");

	/** Thread group for all message poller threads */
	static protected final ThreadGroup GROUP = new ThreadGroup("Poller");

	/** Write a message to the polling log */
	protected void plog(String msg) {
		if(POLL_LOG.isOpen())
			POLL_LOG.log(getName() + " " + msg);
	}

	/** Operation queue */
	protected final OperationQueue queue = new OperationQueue();

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

	/** Hung up flag */
	protected boolean hung_up = false;

	/** Check if the messenger was hung up on */
	public boolean wasHungUp() {
		return hung_up;
	}

	/** Create a new message poller with the open mode specified */
	protected MessagePoller(String name, Messenger m, OpenMode om) {
		super(GROUP, "Poller: " + name);
		setDaemon(true);
		messenger = m;
		open_mode = om;
	}

	/** Create a new message poller with a persistent open mode */
	protected MessagePoller(String name, Messenger m) {
		this(name, m, OpenMode.PERSISTENT);
	}

	/** Set the receive timeout */
	public void setTimeout(int t) throws IOException {
		messenger.setTimeout(t);
	}

	/** Add an operation to the message poller */
	protected void addOperation(Operation op) {
		if(!queue.enqueue(op))
			plog("DROPPING " + op);
	}

	/** Stop polling on this thread */
	public void stopPolling() {
		addOperation(new KillThread());
	}

	/** MessagePoller is a subclass of Thread.  This is the run method. */
	public void run() {
		status = "STARTING";
		plog("STARTING");
		try {
			if(open_mode == OpenMode.PERSISTENT)
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
			if(open_mode == OpenMode.PERSISTENT)
				messenger.close();
			drainQueue();
			plog("STOPPING");
		}
	}

	/** Drain the poll queue */
	protected void drainQueue() {
		queue.close();
		while(queue.hasNext()) {
			Operation o = queue.next();
			o.handleCommError(EventType.QUEUE_DRAINED, status);
			if(hung_up)
				o.setSucceeded();
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
		long start = TimeSteward.currentTimeMillis();
		try {
			if(open_mode == OpenMode.PER_OP)
				messenger.open();
			o.poll(createMessage(o.getController()));
			if(open_mode == OpenMode.PER_OP)
				messenger.close();
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
				long el = sample_load(start);
				plog(oname + " elapsed: " + el);
			}
		}
	}

	/** Handle device contention.  Another operation has the device lock.
	 * Ensure that we don't have a priority inversion problem. */
	private void handleContention(Operation op,DeviceContentionException e){
		Operation oc = e.operation;
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
	protected boolean requeueOperation(Operation op) {
		if(queue.requeue(op))
			return true;
		else {
			plog("DROPPING " + op);
			return false;
		}
	}

	/** Sample the current line load */
	protected long sample_load(long start) {
		long now = TimeSteward.currentTimeMillis();
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
	abstract protected CommMessage createMessage(ControllerImpl c)
		throws EOFException;

	/** Respond to a download request from a controller */
	protected void download(ControllerImpl c, PriorityLevel p) {
		// Subclasses should override this if necessary
	}
}
