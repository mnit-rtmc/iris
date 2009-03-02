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
package us.mn.state.dot.tms.comm.mndot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import us.mn.state.dot.sched.Completer;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.RampMeterImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * Operation to query the status of a ramp meter
 *
 * @author Douglas Lau
 */
public class QueryMeterStatus extends Controller170Operation {

	/** Police panel bit from verify data from 170 */
	static protected final int POLICE_PANEL_BIT = 1 << 4;

	/** Get the controller memory address for a red time interval */
	static protected int getRedAddress(int m, int rate) {
		int a = Address.METER_1_TIMING_TABLE;
		if(m == 2)
			a = Address.METER_2_TIMING_TABLE;
		if(MndotPoller.isAfternoon())
			a += Address.OFF_PM_TIMING_TABLE;
		a += Address.OFF_RED_TIME + (rate * 2);
		return a;
	}

	/** Parse the red time from a BCD byte array */
	static protected int parseRedTime(byte[] data) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		BCD.InputStream is = new BCD.InputStream(bis);
		return is.read16Bit();
	}

	/** 30-Second completer */
	protected final Completer completer;

	/** List of remaining phases of operation */
	protected final LinkedList<Phase> phases = new LinkedList<Phase>();

	/** Create a new query meter status operatoin */
	public QueryMeterStatus(ControllerImpl c, Completer comp) {
		super(DATA_30_SEC, c);
		completer = comp;
	}

	/** Begin the operation */
	public void begin() {
		completer.up();
		phase = new GetStatus();
	}

	/** Phase to get the status of the ramp meters */
	protected class GetStatus extends Phase {

		/** Collect meter data from the controller */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] s = new byte[12];
			mess.add(new MemoryRequest(Address.RAMP_METER_DATA, s));
			mess.getRequest();
			if(meter1 != null)
				parseMeterData(meter1, 1, s, 0);
			if(meter2 != null)
				parseMeterData(meter2, 2,s,Address.OFF_METER_2);
			return phases.poll();
		}
	}

	/** Phase to query a ramp meter red time */
	protected class QueryRedTime extends Phase {

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
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = new byte[2];
			mess.add(new MemoryRequest(address, data));
			mess.getRequest();
			float red = parseRedTime(data) / 10.0f;
			int rate = MndotPoller.calculateReleaseRate(meter, red);
			meter.setRateNotify(rate);
			return phases.poll();
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		completer.down();
		super.cleanup();
	}

	/** Parse meter data and process it */
	protected void parseMeterData(RampMeterImpl meter, int n, byte[] data,
		int base) throws IOException
	{
		updateMeterData(meter, n,
			data[base + Address.OFF_STATUS],
			data[base + Address.OFF_CURRENT_RATE],
			data[base + Address.OFF_GREEN_COUNT_30],
			data[base + Address.OFF_POLICE_PANEL]);
	}

	/** Update meter with the most recent 30-second meter data
	 * @param meter Ramp meter
	 * @param n Meter number (1 or 2)
	 * @param s Meter status
	 * @param r Metering rate
	 * @param g 30-second green count
	 * @param p Police-panel/verify status
	 */
	protected void updateMeterData(RampMeterImpl meter, int n, int s,
		int r, int g, int p) throws IOException
	{
		if(!MeterRate.isValid(r))
			throw new InvalidRateException(r);
		MeterStatus status = new MeterStatus(s);
		if(status.isValid()) {
			boolean police = (p & POLICE_PANEL_BIT) != 0;
			updateMeterStatus(meter, n, status, police, r);
			meter.updateGreenCount(completer.getStamp(), g);
		} else
			throw new InvalidStatusException(s);
	}

	/** Update the status of the ramp meter */
	protected void updateMeterStatus(RampMeterImpl meter, int n,
		MeterStatus status, boolean police, int rate)
	{
		meter.setPolicePanel(police);
		if(!police)
			meter.setManual(status.isManual());
		boolean needs_red_time = false;
		boolean metering = status.isMetering() ||
			MeterRate.isMetering(rate);
		if(metering != meter.isMetering()) {
			needs_red_time = metering;
			if(!metering)
				meter.setRateNotify(null);
		}
		if(status.isManual() || !MeterRate.isCentralControl(rate))
			needs_red_time = true;
		if(MeterRate.isMetering(rate))
			phases.add(new QueryRedTime(meter, n, rate));
	}
}
