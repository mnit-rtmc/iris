/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.ntcip.mib1202.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1202.MIB1202.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Object;

/**
 * This operation queries sample data.
 *
 * @author Douglas Lau
 */
public class OpQuerySamples extends OpController {

	/** NTCIP debug log */
	static private final DebugLog MIB1202_LOG = new DebugLog("mib1202");

	/** Poll period */
	private final int period;

	/** Create a new query samples object */
	public OpQuerySamples(ControllerImpl c, int p) {
		super(PriorityLevel.DEVICE_DATA, c);
		period = p;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseOne() {
		return new QueryDetectors();
	}

	/** Phase to query the detectors */
	protected class QueryDetectors extends Phase {

		/** Query the detectors */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer max_det = maxVehicleDetectors.makeInt();
			mess.add(max_det);
			mess.queryProps();
			logQuery(max_det);
			return null;
		}
	}

	/** Log a property query */
	protected void logQuery(ASN1Object prop) {
		if (MIB1202_LOG.isOpen())
			MIB1202_LOG.log(controller.getName() + ": " + prop);
	}
}
