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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import java.net.URI;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.CommThread;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.MessengerException;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.OpQueue;
import us.mn.state.dot.tms.server.comm.snmp.SNMP;

/**
 * NTCIP thread
 *
 * @author Douglas Lau
 */
public class NtcipThread extends CommThread {

	/** Communication protocol */
	private final CommProtocol protocol;

	/** SNMP message protocol */
	private final SNMP snmp = new SNMP();

	/** Create a new Ntcip thread */
	@SuppressWarnings("unchecked")
	public NtcipThread(NtcipPoller p, OpQueue q, URI s, String u,
		int rt, DebugLog log, CommProtocol cp)
	{
		super(p, q, s, u, rt, log);
		protocol = cp;
	}

	/** Create a messenger */
	@Override
	protected Messenger createMessenger(URI s, String u, int rt)
		throws MessengerException, IOException
	{
		Messenger m = Messenger.create(s, u, rt);
		if (protocol == CommProtocol.NTCIP_B)
			return new HDLCMessenger(m);
		else
			return m;
	}

	/** Create a message for the specified operation.
	 * @param m The messenger.
	 * @param o The operation.
	 * @return New comm message. */
	@Override
	protected CommMessage createCommMessage(Messenger m, OpController o)
		throws IOException
	{
		ControllerImpl c = o.getController();
		return snmp.new Message(m.getOutputStream(c),
			m.getInputStream("", c), c.getPassword());
	}
}
