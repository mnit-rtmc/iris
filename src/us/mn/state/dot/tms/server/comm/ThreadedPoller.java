/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2017  Minnesota Department of Transportation
 * Copyright (C) 2015-2017  SRF Consulting Group
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
import java.util.ArrayList;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * ThreadedPoller is a class polling devices using a CommThread.
 *
 * @author Douglas Lau
 * @author John L. Stanley
 */
public class ThreadedPoller<T extends ControllerProperty>
	implements DevicePoller
{
	/** Operation queue */
	protected final OpQueue<T> queue = new OpQueue<T>();

	/** Poller (comm link) name */
	public final String name;

	/** Default URI scheme */
	protected final URI scheme;

	/** Protocol logger */
	protected final DebugLog logger;

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
		queue.close();
		disconnect();
		drainQueue();
		log("DESTROYED");
	}

	/** Drain the operation queue */
	private void drainQueue() {
		queue.forEach(new OpHandler<T>() {
			public boolean handle(OpController<T> o) {
				o.handleCommError(EventType.QUEUE_DRAINED,
					"DRAINED");
				o.cleanup();
				return true;
			}
		});
	}

	/** Handle error for all operations in queue.
	 * @param et Event type.
	 * @param msg Error message.
	 * @return true If all operations are done. */
	public boolean handleError(final EventType et, final String msg)
		throws InterruptedException
	{
		log("HANDLING " + msg);
		ArrayList<OpController<T>> not_done =
			new ArrayList<OpController<T>>();
		while (true) {
			OpController<T> o = queue.tryNext();
			if (null == o)
				break;
			o.handleCommError(et, msg);
			if (o.isDone())
				o.cleanup();
			else
				not_done.add(o);
		}
		for (OpController<T> o : not_done)
			queue.enqueue(o);
		log("DONE HANDLING " + msg);
		return not_done.isEmpty();
	}

	/** Add an operation to the device poller */
	protected void addOp(OpController<T> op) {
		if (!isConnected()) {
			disconnect();
			createCommThread();
		}
		if (queue.enqueue(op))
			log("ADDING " + op);
		else
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
		disconnect();
	}

	/** Receive timeout (ms) */
	private int timeout;

	/** Set the receive timeout (ms) */
	@Override
	public synchronized void setTimeout(int rt) {
		timeout = rt;
		disconnect();
	}

	/** Comm thread (may be null) */
	private CommThread c_thread;

	/** Get the poller status */
	@Override
	public synchronized String getStatus() {
		return (c_thread != null) ? c_thread.getStatus() : "";
	}

	/** Check if the poller is currently connected */
	@Override
	public synchronized boolean isConnected() {
		return (c_thread != null) && c_thread.isConnected();
	}

	/** COMM_IDLE_DISCONNECT IRIS-system-attribute for this poller
	 * (Change value in constructor of subclasses as needed) */
	protected SystemAttrEnum attrCommIdleDisconnect = null;
	
	/** Get max seconds an idle (non-modem)
	 *  connection should be left open
	 *  (-1 == infinite) */
	@Override
	public int getPollerIdleDisconnectSec() {
		return (attrCommIdleDisconnect == null)
		     ? -1
		     : attrCommIdleDisconnect.getInt();
	}
	
	/** Create the comm thread */
	private synchronized void createCommThread() {
		c_thread = createCommThread(uri, timeout);
		c_thread.start();
		log("THREAD START");
	}

	/** Create a new comm thread */
	protected CommThread<T> createCommThread(String uri, int timeout) {
		return new CommThread<T>(this, queue, scheme, uri, timeout,
			logger);
	}

	/** Disconnect */
	private synchronized void disconnect() {
		if (c_thread != null)
			c_thread.destroy();
		c_thread = null;
		log("THREAD STOP");
	}

	/** Disconnect if idle */
	@Override
	public void disconnectIfIdle() {
		if (queue.isEmpty())
			disconnect();
	}
}
