/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.e6;

import java.io.IOException;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.CommMessageImpl;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.OpController;

/**
 * E6 message.
 *
 * @author Douglas Lau
 */
public class E6Message extends CommMessageImpl<E6Property>
	implements CommMessage<E6Property>
{
	/** E6 thread */
	private final E6Thread thread;

	/** Create a new E6 message.
	 * @param m Messenger to use for communication.
	 * @param o Controller operation.
	 * @param pl Protocol debug log.
	 * @param th E6 thread. */
	public E6Message(Messenger m, OpController<E6Property> o, DebugLog pl,
		E6Thread th)
	{
		super(m, o, pl);
		thread = th;
	}

	/** Send a store packet */
	public void sendStore(E6Property p) throws IOException {
		thread.sendStore(p);
	}

	/** Send a query packet */
	public void sendQuery(E6Property p) throws IOException {
		thread.sendQuery(p);
	}

	/** Get the timeout */
	public int getTimeout() {
		return thread.getTimeout();
	}
}
