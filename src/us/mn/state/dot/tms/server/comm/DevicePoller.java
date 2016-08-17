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
 * DevicePoller is the base class for polling devices (or controllers).
 *
 * @author Douglas Lau
 */
public class DevicePoller<T extends ControllerProperty> {

	/** Default URI for UDP sockets */
	static protected final URI UDP = URI.create("udp:/");

	/** Default URI for TCP sockets */
	static protected final URI TCP = URI.create("tcp:/");

	/** Default URI for HTTP sockets */
	static protected final URI HTTP = URI.create("http:/");

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

	/** Create a device poller */
	protected DevicePoller(String n, URI s, DebugLog l) {
		name = n;
		scheme = s;
		logger = l;
		log("CREATED");
	}

	/** Destroy the poller */
	public void destroy() {
		destroyCommThread();
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
		if (!isConnected()) {
			destroyCommThread();
			createCommThread();
		}
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
	public synchronized void setUri(String u) {
		uri = u;
		destroyCommThread();
	}

	/** Receive timeout (ms) */
	private int timeout;

	/** Set the receive timeout (ms) */
	public synchronized void setTimeout(int rt) {
		timeout = rt;
		destroyCommThread();
	}

	/** Comm thread (may be null) */
	private CommThread c_thread;

	/** Poller status when no comm thread exists */
	private String status = "INIT";

	/** Get the poller status */
	public String getStatus() {
		CommThread ct = c_thread;
		return (ct != null) ? ct.getStatus() : status;
	}

	/** Set the poller status */
	private void setStatus(String s) {
		status = s;
	}

	/** Check if the poller is currently connected */
	public boolean isConnected() {
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

	/** Destroy the comm thread */
	public synchronized void destroyCommThread() {
		setStatus("STOPPED");
		if (c_thread != null)
			c_thread.destroy();
		c_thread = null;
	}

	/** Destroy the comm thread if idle */
	public void destroyIdleCommThread() {
		if (queue.isEmpty())
			destroyCommThread();
	}
}
