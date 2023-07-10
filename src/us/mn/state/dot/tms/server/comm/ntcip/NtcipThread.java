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
import java.util.Random;
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
import us.mn.state.dot.tms.server.comm.snmp.ReqIdGenerator;

/**
 * NTCIP thread
 *
 * @author Douglas Lau
 */
public class NtcipThread extends CommThread {

	/** Default request-ID generator */
	private final ReqIdGenerator req_id_gen = new ReqIdGenerator() {
		/** Maximum SNMP request-id */
		static private final int REQUEST_ID_MAX = 0x7FFFFFFF;

		/** SNMP request-id */
		private int req_id = 0;

		/** Get the next request-ID */
		@Override public int next() {
			req_id = (req_id < REQUEST_ID_MAX) ? req_id + 1 : 1;
			return req_id;
		}
	};

	/** Request-ID generator for Ledstar controllers */
	private final ReqIdGenerator req_id_gen_ledstar = new ReqIdGenerator() {
		/** Maximum request-id to use for Ledstar controllers.
		 *
		 * Some firmware versions encode request-id values greater
		 * than 127 as negative (-128,-127,-126,...) */
		static private final int REQUEST_ID_MAX = 0x7F;

		/** SNMP request-id */
		private int req_id = 0;

		/** Get the next request-ID */
		@Override public int next() {
			req_id = (req_id < REQUEST_ID_MAX) ? req_id + 1 : 1;
			return req_id;
		}
	};

	/** Request-ID generator for Vaisala LX controllers */
	private final ReqIdGenerator req_id_gen_lx = new ReqIdGenerator() {
		/** Random number generator */
		private Random random = new Random();

		/** Get the next request-ID */
		@Override public int next() {
			return random.nextInt(1 << 31);
		}
	};

	/** Check an operation for Ledstar controller */
	static private boolean isLedstar(OpController o) {
		return (o instanceof OpNtcip) && ((OpNtcip) o).isLedstar();
	}

	/** Check an operation for a Vaisala LX controller */
	static private boolean isVaisalaLx(OpController o) {
		return (o instanceof OpNtcip) && ((OpNtcip) o).isVaisalaLx();
	}

	/** Generate a request-ID */
	private int generateReqId(OpController o) {
		if (isLedstar(o))
			return req_id_gen_ledstar.next();
		else if (isVaisalaLx(o))
			return req_id_gen_lx.next();
		else
			return req_id_gen.next();
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
		int req_id = generateReqId(o);
		return snmp.new Message(m.getOutputStream(c),
			m.getInputStream("", c), c.getPassword(), req_id);
	}
}
