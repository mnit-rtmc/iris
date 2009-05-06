/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.tms.LCSArrayImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * Operation to query the status of a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class LCSQueryStatus extends LCSOperation {

	/** LCS to query */
	protected final LCSArrayImpl lcs_array;

	/** Create a new operation to query the LCS */
	public LCSQueryStatus(LCSArrayImpl l) {
		super(DATA_30_SEC, l);
		lcs_array = l;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new QueryStatus();
	}

	/** Phase to query the LCS status */
	protected class QueryStatus extends Phase {

		/** Query the status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] b = new byte[1];
			mess.add(new MemoryRequest(Address.RAMP_METER_DATA, b));
			mess.getRequest();
			lcs_array.setDeployedStatus(b[Address.OFF_STATUS] !=
				MeterStatus.FLASH);
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(!success)
			lcs_array.setStatus(errorStatus);
		super.cleanup();
	}
}
