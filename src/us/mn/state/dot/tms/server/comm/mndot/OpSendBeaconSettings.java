/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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

	/** Beacon */
	private final BeaconImpl beacon;

	/** Create a new send beacon settings operation */
	public OpSendBeaconSettings(PriorityLevel p, BeaconImpl b) {
		super(p, b);
		beacon = b;
	}

	/** Create a new send beacon settings operation */
	public OpSendBeaconSettings(BeaconImpl b) {
		this(PriorityLevel.DOWNLOAD, b);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseTwo() {
		return new SetTimingTable();
	}

	/** Phase to set the timing table for the beacon */
	protected class SetTimingTable extends Phase<MndotProperty> {

		/** Set the timing table for the beacon */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			MemoryProperty p = new MemoryProperty(tableAddress(),
				new byte[54]);
			formatTimingTable(p);
			mess.add(p);
			mess.storeProps();
			return null;
		}
	}

	/** Format a timing table with BCD values */
	private void formatTimingTable(MemoryProperty p) throws IOException {
		final int[] times = { 0x0730, 0x1630 };
		for (int t = Calendar.AM; t <= Calendar.PM; t++) {
			p.format16(0x0001);		// Startup GREEN
			p.format16(0x0001);		// Startup YELLOW
			p.format16(0x0003);		// Metering GREEN
			p.format16(0x0001);		// Metering YELLOW
			p.format16(0x0080);		// HOV preempt
			for (int i = 0; i < 6; i++)
				p.format16(0x0001);	// Metering RED
			p.formatBCD2(MeterRate.OFF);	// TOD rate
			p.format16(times[t]);		// TOD start time
			p.format16(times[t]);		// TOD stop time
		}
	}
}
