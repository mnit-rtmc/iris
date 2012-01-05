/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.tms.server.WarningSignImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Query the status of a warning sign
 *
 * @author Douglas Lau
 */
public class OpQueryWarningStatus extends OpDevice {

	/** Warning sign */
	protected final WarningSignImpl warn;

	/** Create a new warning status poll */
	public OpQueryWarningStatus(WarningSignImpl w) {
		super(PriorityLevel.DATA_30_SEC, w);
		warn = w;
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return new QueryStatus();
	}

	/** Phase to query the warning sign status */
	protected class QueryStatus extends Phase {

		/** Query the warning sign status */
		protected Phase poll(CommMessage mess) throws IOException {
			byte[] b = new byte[1];
			mess.add(new MemoryProperty(Address.RAMP_METER_DATA,b));
			mess.queryProps();
			warn.setDeployedStatus(b[Address.OFF_STATUS] !=
				MeterStatus.FLASH);
			return null;
		}
	}
}
