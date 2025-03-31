/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.MeterAlgorithm;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.TimingTable;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.MeterPoller;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Send meter settings to a 170 controller
 *
 * @author Douglas Lau
 */
public class OpSendMeterSettings extends Op170Device {

	/** HOV preempt time (tenths of a second; BCD) (obsolete) */
	static private final int HOV_PREEMPT = 0x0080;

	/** AM midpoint time (minute-of-day; HHMM format) */
	static private final int AM_MID_TIME = 730;

	/** PM midpoint time (minute-of-day; HHMM format) */
	static private final int PM_MID_TIME = 1630;

	/** Convert minute-of-day (0-1440) to HHMM format */
	static private int minuteHHMM(int v) {
		return 100 * (v / 60) + v % 60;
	}

	/** Ramp meter */
	private final RampMeterImpl meter;

	/** Red times for timing table (tenth of a second) */
	private final int[] table_red = {1, 1};

	/** Meter rates for timing table */
	private final int[] table_rate = {MeterRate.OFF, MeterRate.OFF};

	/** Start times for timing table */
	private final int[] table_start = {AM_MID_TIME, PM_MID_TIME};

	/** Stop times for timing table */
	private final int[] table_stop = {AM_MID_TIME, PM_MID_TIME};

	/** Create a new meter settings operation */
	public OpSendMeterSettings(PriorityLevel p, RampMeterImpl m) {
		super(p, m);
		meter = m;
		if (shouldUpdateTimingTable())
			updateTimingTable();
	}

	/** Check if timing table should be updated */
	private boolean shouldUpdateTimingTable() {
		return (meter.getAlgorithm() != MeterAlgorithm.NONE.ordinal())
		    && !meter.isLocked();
	}

	/** Create a new meter settings operation */
	public OpSendMeterSettings(RampMeterImpl m) {
		this(PriorityLevel.SETTINGS, m);
	}

	/** Update the timing table with active timing plans */
	private void updateTimingTable() {
		Hashtags tags = new Hashtags(meter.getNotes());
		TimingTable table = new TimingTable(tags);
		for (int e = 0; e < 2; e++) {
			int start = table.lookupStart(e);
			int stop = table.lookupStop(e);
			if (start > 0 && stop > 0)
				updateTable(start, stop);
		}
	}

	/** Update one timing table with a time action */
	private void updateTable(int start, int stop) {
		int p = TimeActionHelper.getPeriod(start);
		if (TimeActionHelper.getPeriod(stop) != p)
			return;
		int hhmm = minuteHHMM(start);
		table_start[p] = Math.min(table_start[p], hhmm);
		hhmm = minuteHHMM(stop);
		table_stop[p] = Math.max(table_stop[p], hhmm);
		table_red[p] = RedTime.fromReleaseRate(getTarget(p),
			meter.getMeterType());
		table_rate[p] = MeterRate.TOD;
	}

	/** Get the target release rate for the given period */
	private int getTarget(int p) {
		switch (p) {
		case Calendar.AM:
			return meter.getAmTarget();
		case Calendar.PM:
			return meter.getPmTarget();
		default:
			return 2000;
		}
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseTwo() {
		return new SetTimingTable();
	}

	/** Phase to set the timing table for the ramp meter */
	private class SetTimingTable extends Phase<MndotProperty> {

		/** Set the timing table for the ramp meter */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			MemoryProperty p = new MemoryProperty(tableAddress(),
				new byte[54]);
			formatTimingTable(p);
			mess.add(p);
			mess.storeProps();
			return new ClearVerifies();
		}
	}

	/** Format a timing table with BCD values */
	private void formatTimingTable(MemoryProperty p) throws IOException {
		for (int t = Calendar.AM; t <= Calendar.PM; t++) {
			p.formatBCD4(MeterPoller.STARTUP_GREEN);
			p.formatBCD4(MeterPoller.STARTUP_YELLOW);
			p.formatBCD4(MeterPoller.getGreenTime());
			p.formatBCD4(MeterPoller.getYellowTime());
			p.format16(HOV_PREEMPT);
			for (int i = 0; i < 6; i++)
				p.formatBCD4(table_red[t]);
			p.formatBCD2(table_rate[t]);
			p.formatBCD4(table_start[t]);
			p.formatBCD4(table_stop[t]);
		}
	}

	/** Phase to clear the meter verifies for the ramp meter */
	private class ClearVerifies extends Phase<MndotProperty> {

		/** Clear the meter verifies for the ramp meter */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			int address = getVerifyAddress();
			mess.add(new MemoryProperty(address, new byte[1]));
			mess.storeProps();
			return null;
		}
	}

	/** Get the memory address of the meter verify */
	private int getVerifyAddress() {
		return meterAddress(Address.OFF_POLICE_PANEL);
	}
}
