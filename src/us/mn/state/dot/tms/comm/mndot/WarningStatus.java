/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.WarningSignImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.Device2Operation;

/**
 * Query the status of a warning sign
 *
 * @author Douglas Lau
 */
public class WarningStatus extends Device2Operation {

	/** 30-Second completer */
	protected final Completer completer;

	/** Warning sign */
	protected final WarningSignImpl warn;

	/** Create a new warning status poll */
	public WarningStatus(WarningSignImpl w, Completer comp) {
		super(DATA_30_SEC, w);
		completer = comp;
		warn = w;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		completer.up();
		return new QueryStatus();
	}

	/** Phase to query the warning sign status */
	protected class QueryStatus extends Phase {

		/** Query the warning sign status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] b = new byte[1];
			mess.add(new MemoryRequest(Address.RAMP_METER_DATA,
				b));
			mess.getRequest();
			warn.setDeployedStatus(b[Address.OFF_STATUS] !=
				MeterStatus.FLASH);
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		super.cleanup();
		completer.down();
	}
}
