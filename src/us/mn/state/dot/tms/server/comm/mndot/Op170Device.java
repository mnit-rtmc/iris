/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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

import java.util.Calendar;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.DeviceImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.OpDevice;
import static us.mn.state.dot.tms.server.comm.mndot.Address.RAMP_METER_DATA;

/**
 * 170 Device operation
 *
 * @author Douglas Lau
 */
abstract public class Op170Device extends OpDevice<MndotProperty> {

	/** Test if it is afternoon */
	static private boolean isAfternoon() {
		return TimeSteward.getCalendarInstance().get(Calendar.AM_PM) ==
		       Calendar.PM;
	}

	/** Get the meter number on a controller.
	 * @param pin I/O pin.
	 * @return Meter number (1 or 2) or 0 if unassigned. */
	static private int meterNumber(int pin) {
		switch (pin) {
		case Op170.DEVICE_1_PIN:
			return 1;
		case Op170.METER_2_PIN:
			return 2;
		default:
			return 0;
		}
	}

	/** Get the meter number of the device.
	 * @return Meter number (1 or 2) or 0 if unassigned. */
	protected int meterNumber() {
		return meterNumber(device.getPin());
	}

	/** Get memory address for ramp meter data.
	 * @param off Controller address offset.
	 * @return Controller memory address for meter data. */
	protected int meterAddress(int off) {
		if (meterNumber() == 2)
			return RAMP_METER_DATA + Address.OFF_METER_2 + off;
		else
			return RAMP_METER_DATA + off;
	}

	/** Get memory address of the meter timing table.
	 * @return Controller memory address of meter timing table. */
	protected int tableAddress() {
		if (meterNumber() == 2)
			return Address.METER_2_TIMING_TABLE;
		else
			return Address.METER_1_TIMING_TABLE;
	}

	/** Get memory address of a red time in the current timing table.
	 * @param rate Meter rate index (1-6).
	 * @return Controller memory address of red time interval. */
	protected int redAddress(int rate) {
		int a = tableAddress() + Address.OFF_RED_TIME;
		if (isAfternoon())
			a += Address.OFF_PM_TIMING_TABLE;
		return a + (rate * 2);
	}

	/** Create a new 170 device operation */
	protected Op170Device(PriorityLevel p, DeviceImpl d) {
		super(p, d);
	}
}
