/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.stc;

import java.io.IOException;
import static us.mn.state.dot.tms.DeviceRequest.QUERY_STATUS;
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operatoin to query gate arm status.
 *
 * @author Douglas Lau
 */
public class OpQueryGateStatus extends OpSTC {

	/** STC Poller */
	private final STCPoller poller;

	/** Create a new gate arm query status operation */
	public OpQueryGateStatus(GateArmImpl d, STCPoller p) {
		super(PriorityLevel.DEVICE_DATA, d);
		poller = p;
	}

	/** Create the second phase of the operation */
	protected Phase<STCProperty> phaseTwo() {
		return new QueryStatus();
	}

	/** Phase to query the gate status */
	protected class QueryStatus extends Phase<STCProperty> {

		/** Query the status */
		protected Phase<STCProperty> poll(CommMessage mess)
			throws IOException
		{
			StatusProperty s = new StatusProperty();
			mess.add(s);
			mess.queryProps();
			logQuery(s);
			device.release(operation);
			return phaseOne();
		}
	}
}
