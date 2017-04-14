/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.addco;

import java.io.IOException;
import java.net.URI;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.comm.CommThread;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.MessengerException;
import us.mn.state.dot.tms.server.comm.OpQueue;

/**
 * Addco thread
 *
 * @author Douglas Lau
 */
public class AddcoThread extends CommThread<AddcoProperty> {

	/** Create a new Addco thread */
	public AddcoThread(AddcoPoller p, OpQueue<AddcoProperty> q,
		URI s, String u, int rt, DebugLog log)
	{
		super(p, q, s, u, rt, log);
	}

	/** Create a messenger */
	@Override
	protected Messenger createMessenger(URI s, String u, int rt)
		throws MessengerException, IOException
	{
		Messenger m = Messenger.create(s, u, rt);
		return new AddcoMessenger(m);
	}
}
