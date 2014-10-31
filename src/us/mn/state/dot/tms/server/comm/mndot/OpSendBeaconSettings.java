/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * OpSendBeaconSettings configuration data to a 170 controller
 *
 * @author Douglas Lau
 */
public class OpSendBeaconSettings extends Op170Device {

	/** HOV preempt time (tenths of a second) (obsolete) */
	static private final int HOV_PREEMPT = 80;

	/** AM midpoint time (BCD; minute of day) */
	static private final int AM_MID_TIME = 730;

	/** PM midpoint time (BCD; minute of day) */
	static private final int PM_MID_TIME = 1630;

	/** Beacon */
	private final BeaconImpl beacon;

	/** Create a new send beacon settings operation */
	public OpSendBeaconSettings(BeaconImpl b) {
		super(PriorityLevel.DOWNLOAD, b);
		beacon = b;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseTwo() {
		return new SetTimingTable();
	}

	/** Phase to set the timing table for the beacon */
	protected class SetTimingTable extends Phase<MndotProperty> {

		/** Set the timing table for the beacon */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			int a = tableAddress();
			MemoryProperty prop = createTimingTableProperty(a);
			mess.add(prop);
			mess.storeProps();
			return null;
		}
	}

	/** Create a timing table property for the beacon */
	private MemoryProperty createTimingTableProperty(int address)
		throws IOException
	{
		int[] times = {AM_MID_TIME, PM_MID_TIME};
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BCDOutputStream bcd = new BCDOutputStream(os);
		for (int t = Calendar.AM; t <= Calendar.PM; t++) {
			bcd.write4(1);			// Startup GREEN
			bcd.write4(1);			// Startup YELLOW
			bcd.write4(3);			// Metering GREEN
			bcd.write4(1);			// Metering YELLOW
			bcd.write4(HOV_PREEMPT);
			for (int i = 0; i < 6; i++)
				bcd.write4(1);		// Metering RED
			bcd.write2(MeterRate.OFF);	// TOD rate
			bcd.write4(times[t]);		// TOD start time
			bcd.write4(times[t]);		// TOD stop time
		}
		return new MemoryProperty(address, os.toByteArray());
	}
}
