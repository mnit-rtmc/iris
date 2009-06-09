/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TimingPlan;
import us.mn.state.dot.tms.TimingPlanHelper;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.MeterPoller;

/**
 * Send meter settings to a 170 controller
 *
 * @author Douglas Lau
 */
public class MeterSettings extends OpDevice {

	/** Startup green time (tenths of a second) */
	static protected final int STARTUP_GREEN = 80;

	/** Startup yellow time (tenths of a second) */
	static protected final int STARTUP_YELLOW = 50;

	/** HOV preempt time (tenths of a second) (obsolete) */
	static protected final int HOV_PREEMPT = 80;

	/** AM midpoint time (BCD; minute of day) */
	static protected final int AM_MID_TIME = 730;

	/** PM midpoint time (BCD; minute of day) */
	static protected final int PM_MID_TIME = 1630;

	/** Minute of 12 Noon in day */
	static protected final int NOON = 12 * 60;

	/** Check if a timing plan is for the given peak period */
	static protected boolean checkPeriod(TimingPlan plan, int period) {
		if(period == Calendar.AM && plan.getStopMin() <= NOON)
			return true;
		if(period == Calendar.PM && plan.getStartMin() >= NOON)
			return true;
		return false;
	}

	/** Get the system meter green time */
	static protected int getGreenTime() {
		float g = SystemAttrEnum.METER_GREEN_SECS.getFloat();
		return Math.round(g * 10);
	}

	/** Get the system meter yellow time */
	static protected int getYellowTime() {
		float g = SystemAttrEnum.METER_YELLOW_SECS.getFloat();
		return Math.round(g * 10);
	}

	/** Convert minute-of-day (0-1440) to 4-digit BCD */
	static protected int minuteBCD(int v) {
		return 100 * (v / 60) + v % 60;
	}

	/** Ramp meter */
	protected final RampMeterImpl meter;

	/** Red times for timing table */
	protected final int[] table_red = {1, 1};

	/** Meter rates for timing table */
	protected final int[] table_rate = {MeterRate.FLASH, MeterRate.FLASH};

	/** Start times for timing table */
	protected final int[] table_start = {AM_MID_TIME, PM_MID_TIME};

	/** Stop times for timing table */
	protected final int[] table_stop = {AM_MID_TIME, PM_MID_TIME};

	/** Create a new meter settings operation */
	public MeterSettings(RampMeterImpl m) {
		super(DOWNLOAD, m);
		meter = m;
		updateTimingTable();
	}

	/** Update the timing table with active timing plans */
	protected void updateTimingTable() {
		TimingPlanHelper.find(new Checker<TimingPlan>() {
			public boolean check(TimingPlan p) {
				if(p.getActive() && p.getDevice() == meter)
					updateTable(p);
				return false;
			}
		});
	}

	/** Update one timing table with a timing plan */
	protected void updateTable(TimingPlan p) {
		for(int t = Calendar.AM; t <= Calendar.PM; t++) {
			if(checkPeriod(p, t)) {
				int sta = minuteBCD(p.getStartMin());
				int sto = minuteBCD(p.getStopMin());
				float r = MndotPoller.calculateRedTime(meter,
					p.getTarget());
				table_red[t] = Math.round(r * 10);
				table_rate[t] = MeterRate.TOD;
				table_start[t] = Math.min(table_start[t], sta);
				table_stop[t] = Math.max(table_stop[t], sto);
			}
		}
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new ResetWatchdogMonitor();
	}

	/** Phase to reset the watchdog monitor */
	protected class ResetWatchdogMonitor extends Phase {

		/** Reset the watchdog monitor */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = {Address.WATCHDOG_BITS};
			mess.add(new MemoryRequest(
				Address.SPECIAL_FUNCTION_OUTPUTS + 2, data));
			mess.setRequest();
			return new ClearWatchdogMonitor();
		}
	}

	/** Phase to clear the watchdog monitor */
	protected class ClearWatchdogMonitor extends Phase {

		/** Clear the watchdog monitor */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = new byte[1];
			mess.add(new MemoryRequest(
				Address.SPECIAL_FUNCTION_OUTPUTS + 2, data));
			mess.setRequest();
			return new SetCommFail();
		}
	}

	/** Phase to set the comm fail time */
	protected class SetCommFail extends Phase {

		/** Set the comm fail time */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = {MeterPoller.COMM_FAIL_THRESHOLD};
			mess.add(new MemoryRequest(Address.COMM_FAIL, data));
			mess.setRequest();
			return new SetTimingTable();
		}
	}

	/** Phase to set the timing table for the ramp meter */
	protected class SetTimingTable extends Phase {

		/** Set the timing table for the ramp meter */
		protected Phase poll(AddressedMessage mess) throws IOException {
			int a = Address.METER_1_TIMING_TABLE;
			mess.add(createTimingTableRequest(a));
			mess.setRequest();
			return new ClearVerifies();
		}
	}

	/** Create a timing table request for the meter */
	protected Request createTimingTableRequest(int address)
		throws IOException
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BCD.OutputStream bcd = new BCD.OutputStream(os);
		for(int t = Calendar.AM; t <= Calendar.PM; t++) {
			bcd.write16Bit(STARTUP_GREEN);
			bcd.write16Bit(STARTUP_YELLOW);
			bcd.write16Bit(getGreenTime());
			bcd.write16Bit(getYellowTime());
			bcd.write16Bit(HOV_PREEMPT);
			for(int i = 0; i < 6; i++)
				bcd.write16Bit(table_red[t]);
			bcd.write8Bit(table_rate[t]);
			bcd.write16Bit(table_start[t]);
			bcd.write16Bit(table_stop[t]);
		}
		return new MemoryRequest(address, os.toByteArray());
	}

	/** Phase to clear the meter verifies for the ramp meter */
	protected class ClearVerifies extends Phase {

		/** Clear the meter verifies for the ramp meter */
		protected Phase poll(AddressedMessage mess) throws IOException {
			int address = Address.RAMP_METER_DATA +
				Address.OFF_POLICE_PANEL;
			mess.add(new MemoryRequest(address, new byte[1]));
			mess.setRequest();
			return null;
		}
	}
}
