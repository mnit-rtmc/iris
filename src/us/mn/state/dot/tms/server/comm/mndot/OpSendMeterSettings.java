/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2011  Minnesota Department of Transportation
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
import java.util.LinkedList;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanState;
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.MeterActionHelper;
import us.mn.state.dot.tms.MeterAlgorithm;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.MeterPoller;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Send meter settings to a 170 controller
 *
 * @author Douglas Lau
 */
public class OpSendMeterSettings extends OpDevice {

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
	public OpSendMeterSettings(RampMeterImpl m) {
		super(PriorityLevel.DOWNLOAD, m);
		meter = m;
		if(meter.getAlgorithm() != MeterAlgorithm.NONE.ordinal())
			updateTimingTable();
	}

	/** Update the timing table with active timing plans */
	protected void updateTimingTable() {
		final LinkedList<ActionPlan> plans =
			new LinkedList<ActionPlan>();
		MeterActionHelper.find(new Checker<MeterAction>() {
			public boolean check(MeterAction ma) {
				if(ma.getRampMeter() == meter &&
				   ActionPlanState.isDeployed(ma.getState()))
					plans.add(ma.getActionPlan());
				return false;
			}
		});
		for(ActionPlan ap: plans) {
			if(ap.getActive())
				updateTable(ap);
		}
	}

	/** Update one timing table with an action plan */
	protected void updateTable(final ActionPlan ap) {
		TimeActionHelper.find(new Checker<TimeAction>() {
			public boolean check(TimeAction ta) {
				if(ta.getActionPlan() == ap)
					updateTable(ta);
				return false;
			}
		});
	}

	/** Update one timing table with a time action */
	private void updateTable(TimeAction ta) {
		int p = TimeActionHelper.getPeriod(ta);
		int min = minuteBCD(ta.getMinute());
		float r = MndotPoller.calculateRedTime(meter, getTarget(p));
		table_red[p] = Math.round(r * 10);
		table_rate[p] = MeterRate.TOD;
		if(ta.getDeploy())
			table_start[p] = Math.min(table_start[p], min);
		else
			table_stop[p] = Math.max(table_stop[p], min);
	}

	/** Get the target release rate for the given period */
	private int getTarget(int p) {
		switch(p) {
		case Calendar.AM:
			return meter.getAmTarget();
		case Calendar.PM:
			return meter.getPmTarget();
		default:
			return 2000;
		}
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new ResetWatchdogMonitor();
	}

	/** Phase to reset the watchdog monitor */
	protected class ResetWatchdogMonitor extends Phase {

		/** Reset the watchdog monitor */
		protected Phase poll(CommMessage mess) throws IOException {
			byte[] data = {Address.WATCHDOG_BITS};
			mess.add(new MemoryProperty(
				Address.SPECIAL_FUNCTION_OUTPUTS + 2, data));
			mess.storeProps();
			return new ClearWatchdogMonitor();
		}
	}

	/** Phase to clear the watchdog monitor */
	protected class ClearWatchdogMonitor extends Phase {

		/** Clear the watchdog monitor */
		protected Phase poll(CommMessage mess) throws IOException {
			byte[] data = new byte[1];
			mess.add(new MemoryProperty(
				Address.SPECIAL_FUNCTION_OUTPUTS + 2, data));
			mess.storeProps();
			return new SetCommFail();
		}
	}

	/** Phase to set the comm fail time */
	protected class SetCommFail extends Phase {

		/** Set the comm fail time */
		protected Phase poll(CommMessage mess) throws IOException {
			byte[] data = {MeterPoller.COMM_FAIL_THRESHOLD};
			mess.add(new MemoryProperty(Address.COMM_FAIL, data));
			mess.storeProps();
			return new SetTimingTable();
		}
	}

	/** Phase to set the timing table for the ramp meter */
	protected class SetTimingTable extends Phase {

		/** Set the timing table for the ramp meter */
		protected Phase poll(CommMessage mess) throws IOException {
			int a = getTableAddress();
			mess.add(createTimingTableProperty(a));
			mess.storeProps();
			return new ClearVerifies();
		}
	}

	/** Get the memory address of the meter timing table */
	protected int getTableAddress() {
		if(meter.getPin() == Op170.METER_2_PIN)
			return Address.METER_2_TIMING_TABLE;
		else
			return Address.METER_1_TIMING_TABLE;
	}

	/** Create a timing table property for the meter */
	protected MndotProperty createTimingTableProperty(int address)
		throws IOException
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BCDOutputStream bcd = new BCDOutputStream(os);
		for(int t = Calendar.AM; t <= Calendar.PM; t++) {
			bcd.write4(STARTUP_GREEN);
			bcd.write4(STARTUP_YELLOW);
			bcd.write4(getGreenTime());
			bcd.write4(getYellowTime());
			bcd.write4(HOV_PREEMPT);
			for(int i = 0; i < 6; i++)
				bcd.write4(table_red[t]);
			bcd.write2(table_rate[t]);
			bcd.write4(table_start[t]);
			bcd.write4(table_stop[t]);
		}
		return new MemoryProperty(address, os.toByteArray());
	}

	/** Phase to clear the meter verifies for the ramp meter */
	protected class ClearVerifies extends Phase {

		/** Clear the meter verifies for the ramp meter */
		protected Phase poll(CommMessage mess) throws IOException {
			int address = getVerifyAddress();
			mess.add(new MemoryProperty(address, new byte[1]));
			mess.storeProps();
			return null;
		}
	}

	/** Get the memory address of the meter timing table */
	protected int getVerifyAddress() {
		int a = Address.RAMP_METER_DATA + Address.OFF_POLICE_PANEL;
		if(meter.getPin() == Op170.METER_2_PIN)
			a += Address.OFF_METER_2;
		return a;
	}
}
