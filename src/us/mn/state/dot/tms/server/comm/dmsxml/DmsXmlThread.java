/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dmsxml;

import java.io.IOException;
import java.net.URI;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.CommThread;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.OpQueue;

/**
 * DMSXML thread.
 *
 * @author Douglas Lau
 */
public class DmsXmlThread extends CommThread {

	/** Create a new dmsxml thread */
	@SuppressWarnings("unchecked")
	public DmsXmlThread(DmsXmlPoller p, OpQueue q, URI s, String u,
		int rt, DebugLog log)
	{
		super(p, q, s, u, rt, log);
	}

	/** Create a new message for the specified operation.
	 * @see CommThread.doPoll().
	 *
	 * @param m The messenger.
	 * @param o The controller operation.
	 * @return A newly created Message.
	 * @throws IOException */
	@Override
	protected CommMessage createCommMessage(Messenger m, OpController o)
		throws IOException
	{
		ControllerImpl c = o.getController();
		return new Message(m.getOutputStream(c),
				   m.getInputStream("", c));
	}
}
