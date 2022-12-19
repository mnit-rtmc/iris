/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2022  SRF Consulting Group
 * Copyright (C) 2021-2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ndorv5;

import java.io.IOException;
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query gate arm status.
 *
 * @author John L. Stanley - SRF Consulting
 * @author Douglas Lau
 */
@SuppressWarnings("rawtypes")
public class OpQueryGateStatus extends OpGateNdorV5 {

	/** Create a new gate arm query status operation */
	@SuppressWarnings("unchecked")
	public OpQueryGateStatus(GateArmImpl d) {
		// Don't require exclusive access to device.  This prevents
		// priority from being changed due to DeviceContentionException.
		super(PriorityLevel.SETTINGS, d, false); // non-exclusive
		if (sGateArm == null) {
			prop = null;
			return;
		}
		// NDORv5 "Retrieve Gate Status" command
		prop = new GateNdorV5Property("*S"+sGateArm+"#\r\n");
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return (prop != null) ? new QueryStatus() : null;
	}

	/** Phase to query the gate status */
	protected class QueryStatus extends Phase {

		/** Query the status */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess)
			throws IOException
		{
			mess.add(prop);
			mess.queryProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		super.cleanup();
		if (!isSuccess())
			gate_arm.checkTimeout();
	}
}
