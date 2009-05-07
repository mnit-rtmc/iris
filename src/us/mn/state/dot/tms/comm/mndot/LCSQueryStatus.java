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

	/** Device on/off status ("metering" status) */
	protected final byte[] status = new byte[1];

	/** Special function output buffer */
	protected final byte[] outputs = new byte[4];

	/** Create a new operation to query the LCS */
	public LCSQueryStatus(LCSArrayImpl l) {
		super(DATA_30_SEC, l);
	}

	/** Begin the operation */
	public void begin() {
		phase = new QueryStatus();
	}

	/** Phase to query the LCS status */
	protected class QueryStatus extends Phase {

		/** Query the status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new MemoryRequest(Address.RAMP_METER_DATA,
				status));
			mess.getRequest();
			if(isDark())
				return null;
			else
				return new QueryOutputs();
		}
	}

	/** Phase to query the LCS special function outputs */
	protected class QueryOutputs extends Phase {

		/** Query the outputs */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new MemoryRequest(
				Address.SPECIAL_FUNCTION_OUTPUTS, outputs));
			mess.getRequest();
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(success) {
			lcs_array.setDeployedStatus(b[Address.OFF_STATUS] !=
				MeterStatus.FLASH);
		}
		super.cleanup();
	}
}
