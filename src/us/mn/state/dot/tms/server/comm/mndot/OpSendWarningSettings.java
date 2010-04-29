/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import us.mn.state.dot.tms.server.WarningSignImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;

/**
 * OpSendWarningSettings configuration data to a 170 controller
 *
 * @author Douglas Lau
 */
public class OpSendWarningSettings extends OpDevice {

	/** HOV preempt time (tenths of a second) (obsolete) */
	static protected final int HOV_PREEMPT = 80;

	/** AM midpoint time (BCD; minute of day) */
	static protected final int AM_MID_TIME = 730;

	/** PM midpoint time (BCD; minute of day) */
	static protected final int PM_MID_TIME = 1630;

	/** Warning sign */
	protected final WarningSignImpl warning_sign;

	/** Create a new send warning settings operation */
	public OpSendWarningSettings(WarningSignImpl w) {
		super(DOWNLOAD, w);
		warning_sign = w;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new SetTimingTable();
	}

	/** Phase to set the timing table for the warning sign */
	protected class SetTimingTable extends Phase {

		/** Set the timing table for the warning sign */
		protected Phase poll(AddressedMessage mess) throws IOException {
			int a = Address.METER_1_TIMING_TABLE;
			mess.add(createTimingTableRequest(a));
			mess.setRequest();
			return null;
		}
	}

	/** Create a timing table request for the warning sign */
	protected MndotRequest createTimingTableRequest(int address)
		throws IOException
	{
		int[] times = {AM_MID_TIME, PM_MID_TIME};
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BCDOutputStream bcd = new BCDOutputStream(os);
		for(int t = Calendar.AM; t <= Calendar.PM; t++) {
			bcd.write4(1);			// Startup GREEN
			bcd.write4(1);			// Startup YELLOW
			bcd.write4(3);			// Metering GREEN
			bcd.write4(1);			// Metering YELLOW
			bcd.write4(HOV_PREEMPT);
			for(int i = 0; i < 6; i++)
				bcd.write4(1);		// Metering RED
			bcd.write2(MeterRate.FLASH);	// TOD rate
			bcd.write4(times[t]);		// TOD start time
			bcd.write4(times[t]);		// TOD stop time
		}
		return new MemoryRequest(address, os.toByteArray());
	}
}
