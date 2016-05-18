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
	static protected final String UDP = "udp:/";

	/** Default URI for TCP sockets */
	static protected final String TCP = "tcp:/";

	/** Default URI for HTTP sockets */
	static protected final String HTTP = "http:/";

	/** Operation queue */
	protected final OpQueue<T> queue = new OpQueue<T>();

	/** Poller (comm link) name */
	public final String name;

	/** Default URI scheme */
	public final String d_uri;

	/** Protocol logger */
	public final DebugLog logger;

	/** Write a message to the protocol log */
	public void log(String msg) {
		if (logger.isOpen())
			logger.log(name + " " + msg);
	}

	/** Create a device poller */
	protected DevicePoller(String n, String du, DebugLog l) {
		name = n;
		d_uri = du;
		logger = l;
		log("CREATED");
	}

	/** Destroy the poller */
	public void destroy() {
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
		if (!queue.enqueue(op))
			log("DROPPING " + op);
	}

	/** Respond to a settings request from a controller */
	public void sendSettings(ControllerImpl c, PriorityLevel p) {
		// Subclasses should override this if necessary
	}

	/** Create a comm thread */
	public CommThread<T> createCommThread(String uri, int timeout)
		throws IOException
	{
		return new CommThread<T>(this, queue, Messenger.create(d_uri,
			uri, timeout));
	}
}
