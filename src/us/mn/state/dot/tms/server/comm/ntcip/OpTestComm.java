/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Object;

/**
 * This operation tests communication.
 *
 * @author Douglas Lau
 */
public class OpTestComm extends OpController {

	/** Debug logger */
	private final DebugLog log;

	/** Create a new test communication operation */
	public OpTestComm(ControllerImpl c, DebugLog l) {
		super(PriorityLevel.DIAGNOSTIC, c);
		log = l;
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase phaseOne() {
		return new TestCommunication();
	}

	/** Phase to test communication */
	private class TestCommunication extends Phase {

		/** Test communication */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer m = MIB1201.globalMaxModules.makeInt();
			mess.add(m);
			mess.queryProps();
			logQuery(m);
			return controller.isTesting() ? this : null;
		}
	}

	/** Log a property query */
	private void logQuery(ASN1Object prop) {
		if (log.isOpen())
			log.log(controller.getName() + ": " + prop);
	}
}
