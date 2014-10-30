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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query the status of a ramp meter
 *
 * @author Douglas Lau
 */
public class OpQueryMeterStatus extends Op170 {

	/** Police panel bit from verify data from 170 */
	static protected final int POLICE_PANEL_BIT = 1 << 4;

	/** Parse the red time from a BCD byte array */
	static private int parseRedTime(byte[] data) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		BCDInputStream is = new BCDInputStream(bis);
		return is.read4();
	}

	/** List of remaining phases of operation */
	private final LinkedList<Phase<MndotProperty>> phases =
		new LinkedList<Phase<MndotProperty>>();

	/** Create a new query meter status operatoin */
	public OpQueryMeterStatus(ControllerImpl c) {
		super(PriorityLevel.DATA_30_SEC, c);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseOne() {
		return new GetStatus();
	}

	/** Phase to get the status of the ramp meters */
	protected class GetStatus extends Phase<MndotProperty> {

		/** Collect meter data from the controller */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			byte[] s = new byte[12];
			MemoryProperty stat_mem = new MemoryProperty(
				Address.RAMP_METER_DATA, s);
			mess.add(stat_mem);
			mess.queryProps();
			long stamp = TimeSteward.currentTimeMillis();
			if (meter1 != null)
				parseMeterData(meter1, 1, s, 0, stamp);
			if (meter2 != null) {
				parseMeterData(meter2, 2, s,
					Address.OFF_METER_2, stamp);
			}
			return phases.poll();
		}
	}

	/** Phase to query a ramp meter red time */
	protected class QueryRedTime extends Phase<MndotProperty> {

		/** Ramp meter in question */
		protected final RampMeterImpl meter;

		/** Controller address of BCD red time */
		protected final int address;

		/** Create a new phase to query the ramp meter red time */
		protected QueryRedTime(RampMeterImpl m, int n, int rate) {
			meter = m;
			address = getRedAddress(n, rate);
		}

		/** Query the meter red time */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			byte[] data = new byte[2];
			MemoryProperty red_mem = new MemoryProperty(address,
				data);
			mess.add(red_mem);
			mess.queryProps();
			float red = parseRedTime(data) / 10.0f;
			int rate = RedTime.toReleaseRate(red,
				meter.getMeterType());
			meter.setRateNotify(rate);
			return phases.poll();
		}
	}

	/** Parse meter data and process it */
	protected void parseMeterData(RampMeterImpl meter, int n, byte[] data,
		int base, long stamp) throws IOException
	{
		updateMeterData(meter, n, stamp,
			data[base + Address.OFF_STATUS],
			data[base + Address.OFF_CURRENT_RATE],
			data[base + Address.OFF_GREEN_COUNT_30],
			data[base + Address.OFF_POLICE_PANEL]);
	}

	/** Update meter with the most recent 30-second meter data.
	 * @param meter Ramp meter.
	 * @param n Meter number (1 or 2).
	 * @param stamp Time stamp.
	 * @param s Meter status.
	 * @param r Metering rate.
	 * @param g 30-second green count.
	 * @param p Police-panel/verify status. */
	private void updateMeterData(RampMeterImpl meter, int n, long stamp,
		int s, int r, int g, int p) throws IOException
	{
		checkMeterState(s, r);
		boolean police = (p & POLICE_PANEL_BIT) != 0;
		updateMeterStatus(meter, n, s, police, r);
		meter.updateGreenCount(stamp, adjustGreenCount(meter, g));
	}

	/** Check meter status and rate for valid values.
	 * @param s Meter status code.
	 * @param r Meter rate index. */
	private void checkMeterState(int s, int r) throws InvalidStateException{
		if (!MeterStatus.isValid(s) ||
		    !MeterRate.isValid(r) ||
		    MeterStatus.isMetering(s) != MeterRate.isMetering(r))
			throw new InvalidStateException(s, r);
	}

	/** Update the status of the ramp meter */
	private void updateMeterStatus(RampMeterImpl meter, int n,
		int s, boolean police, int rate)
	{
		meter.setPolicePanel(police);
		meter.setManual(MeterStatus.isManual(s));
		if (MeterRate.isMetering(rate))
			phases.add(new QueryRedTime(meter, n, rate));
		else
			meter.setRateNotify(null);
	}
}
