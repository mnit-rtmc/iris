/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cpark;

import java.io.IOException;
import java.net.URI;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.comm.CommThread;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.MessengerException;
import us.mn.state.dot.tms.server.comm.OpQueue;

/**
 * CPark thread, used to provide a Messenger with x-access-token header.
 *
 * @author Douglas Lau
 */
public class CParkThread extends CommThread<CParkProp> {

	/** Create a new CPark thread */
	public CParkThread(CParkPoller p, OpQueue<CParkProp> q, URI s,
		String u, int rt, int nrd, DebugLog log)
	{
		super(p, q, s, u, rt, nrd, log);
	}

	/** Create a messenger */
	@Override
	protected Messenger createMessenger(URI s, String u, int rt, int nrd)
		throws MessengerException, IOException
	{
		return new TokenMessenger(
			Messenger.create(s, u, rt, nrd),
			Messenger.createURI(s, u).toURL(),
			rt
		);
	}
}
