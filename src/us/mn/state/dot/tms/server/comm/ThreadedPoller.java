/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2016  Minnesota Department of Transportation
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
import java.net.URI;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * ThreadedPoller is a class polling devices using a CommThread.
 *
 * @author Douglas Lau
 */
public class ThreadedPoller<T extends ControllerProperty>
	implements DevicePoller<T>
{
	/** Operation queue */
	protected final OpQueue<T> queue = new OpQueue<T>();

	/** Poller (comm link) name */
	public final String name;

	/** Default URI scheme */
	public final URI scheme;

	/** Protocol logger */
	public final DebugLog logger;

	/** Write a message to the protocol log */
	public void log(String msg) {
		if (logger.isOpen())
			logger.log(name + " " + msg);
	}

	/** Create a threaded device poller */
	protected ThreadedPoller(String n, URI s, DebugLog l) {
		name = n;
		scheme = s;
		logger = l;
		log("CREATED");
	}

	/** Destroy the poller */
	@Override
	public void destroy() {
		stopPolling();
		queue.close();
		drainQueue();
		log("DESTROYED");
	}

	/** Drain the operation queue */
	private void drainQueue() {
		queue.forEach(new OpHandler<T>() {
			public void handle(PriorityLevel prio,
				OpController<T> o)
			{
				o.handleCommError(EventType.QUEUE_DRAINED,
					"DRAINED");
				o.cleanup();
			}
		});
	}

	/** Add an operation to the device poller */
	protected void addOp(OpController<T> op) {
		if (!isStarted())
			createCommThread();
		if (!queue.enqueue(op))
			log("DROPPING " + op);
	}

	/** Respond to a settings request from a controller */
	public void sendSettings(ControllerImpl c, PriorityLevel p) {
		// Subclasses should override this if necessary
	}

	/** Remote URI */
	private String uri = "";

	/** Set the remote URI */
	@Override
	public synchronized void setUri(String u) {
		uri = u;
		stopPolling();
	}

	/** Receive timeout (ms) */
	private int timeout;

	/** Set the receive timeout (ms) */
	@Override
	public synchronized void setTimeout(int rt) {
		timeout = rt;
		stopPolling();
	}

	/** Comm thread (may be null) */
	private CommThread c_thread;

	/** Check if the comm thread has started */
	private synchronized boolean isStarted() {
		return c_thread != null;
	}

	/** Poller status when no comm thread exists */
	private String status = "INIT";

	/** Get the poller status */
	@Override
	public synchronized String getStatus() {
		CommThread ct = c_thread;
		return (ct != null) ? ct.getStatus() : status;
	}

	/** Set the poller status */
	private void setStatus(String s) {
		status = s;
	}

	/** Check if the poller is currently connected */
	@Override
	public synchronized boolean isConnected() {
		CommThread ct = c_thread;
		return (ct != null) && ct.isAlive();
	}

	/** Create the comm thread */
	private synchronized void createCommThread() {
		try {
			c_thread = createCommThread(uri, timeout);
			c_thread.start();
		}
		catch (IOException e) {
			setStatus("I/O error: " + e.getMessage());
			c_thread = null;
		}
	}

	/** Create a new comm thread */
	protected CommThread<T> createCommThread(String uri, int timeout)
		throws IOException
	{
		return new CommThread<T>(this, queue, scheme, uri, timeout);
	}

	/** Stop polling */
	@Override
	public synchronized void stopPolling() {
		setStatus("STOPPED");
		if (c_thread != null)
			c_thread.destroy();
		c_thread = null;
	}

	/** Stop polling if idle */
	@Override
	public void stopPollingIfIdle() {
		if (queue.isEmpty())
			stopPolling();
	}
}
