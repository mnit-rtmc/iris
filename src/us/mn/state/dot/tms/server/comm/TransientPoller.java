/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2016  Minnesota Department of Transportation
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

/**
 * TransientPoller is a MessagePoller which causes equal operations to be
 * replaced instead of rejected.  It is useful for PTZ pollers (which consist
 * of transient PTZ commands only).
 *
 * @author Douglas Lau
 */
abstract public class TransientPoller<T extends ControllerProperty>
	extends MessagePoller<T>
{
	/** Create a new transient poller */
	protected TransientPoller(String name, Messenger m) {
		super(name, m);
	}

	/** Add an operation to the transient poller */
	@Override
	protected void addOperation(final OpController<T> op) {
		queue.forEach(new OpHandler<T>() {
			public void handle(PriorityLevel p, OpController<T> o) {
				if (o.equals(op))
					o.setSucceeded();
			}
		});
		super.addOperation(op);
	}
}
