/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.DebugLog;

/**
 * MessagePoller is an abstract class which represents a communication channel 
 * with priority-queued polling.  Subclasses are MndotPoller, NtcipPoller, etc.
 *
 * @author Douglas Lau
 */
abstract public class MessagePoller extends Thread {

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

	/** Add an operation to the message poller */
	public void addOperation(Operation o) {
		if(queue.isClogged()) {
			if(o.getPriority() >= Operation.DEVICE_DATA) {
				if(POLL_LOG.isOpen()) {
					POLL_LOG.log(getName() +
						", DROPPING: " + o);
				}
				return;
			}
		}
		o.begin();
		queue.add(o);
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
		try {
			messenger.open();
			status = "";
			performOperations();
			status = "CLOSING";
		}
		catch(IOException e) {
			status = e.getMessage();
		}
		messenger.close();
		drainQueue();
	}

	/** Drain the poll queue */
	protected void drainQueue() {
		IOException closing = new IOException(status);
		while(queue.hasNext()) {
			Operation o = queue.next();
			o.handleException(closing);
			o.cleanup();
		}
	}

	/** Perform operations on the poll queue */
	protected void performOperations() throws IOException {
		while(true) {
			Operation o = queue.next();
			if(o instanceof KillThread)
				break;
			if(o instanceof ControllerOperation)
				doPoll((ControllerOperation)o);
		}
	}

	/** Perform one poll for an operation */
	protected synchronized void doPoll(final ControllerOperation o)
		throws IOException
	{
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
				queue.add(oc);
			}
		}
		catch(DownloadRequestException e) {
			download(o);
		}
		catch(ParsingException e) {
			o.handleException(e);
			messenger.drain();
		}
		catch(SocketTimeoutException e) {
			o.handleException(e);
		}
		catch(NumberFormatException e) {
			o.handleException(new IOException(
				"NUMBER FORMAT EXCEPTION"));
		}
		finally {
			if(o.isDone())
				o.cleanup();
			else
				queue.add(o);
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
	protected void download(ControllerOperation o) {
		download(o.getController(), false, o.getPriority());
	}

	/** Perform a controller download */
	public void download(ControllerImpl c, boolean reset) {
		download(c, reset, Operation.DOWNLOAD);
	}

	/** Perform a controller download */
	abstract public void download(ControllerImpl c, boolean reset, int p);

	/** Perform a 30-second poll */
	abstract public void poll30Second(ControllerImpl c, Completer comp);

	/** Perform a 5-minute poll */
	abstract public void poll5Minute(ControllerImpl c, Completer comp);

	/** Start a test for the given controller */
	abstract public DiagnosticOperation startTest(ControllerImpl c);
}
