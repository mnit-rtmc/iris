/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2023  Minnesota Department of Transportation
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

	/** Maximum request-id to use for Ledstar controllers.
	 *
	 * Some firmware versions encode request-id values greater
	 * than 127 as negative (-128,-127,-126,...) */
	static private final int REQUEST_ID_MAX_LEDSTAR = 0x7F;

	/** Check an operation for Ledstar controller */
	static private boolean isLedstar(OpController o) {
		return (o instanceof OpNtcip) && ((OpNtcip) o).isLedstar();
	}

	/** Communication protocol */
	private final CommProtocol protocol;

	/** SNMP message protocol */
	private final SNMP snmp = new SNMP();

	/** Create a new Ntcip thread */
	@SuppressWarnings("unchecked")
	public NtcipThread(NtcipPoller p, OpQueue q, URI s, String u,
		int rt, int nrd, DebugLog log, CommProtocol cp)
	{
		super(p, q, s, u, rt, nrd, log);
		protocol = cp;
	}

	/** Create a messenger */
	@Override
	protected Messenger createMessenger(URI s, String u, int rt, int nrd)
		throws MessengerException, IOException
	{
		Messenger m = Messenger.create(s, u, rt, nrd);
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
		if (isLedstar(o))
			return snmp.new Message(m.getOutputStream(c),
				m.getInputStream("", c), c.getPassword(),
				REQUEST_ID_MAX_LEDSTAR);
		else
			return snmp.new Message(m.getOutputStream(c),
				m.getInputStream("", c), c.getPassword());
	}
}
