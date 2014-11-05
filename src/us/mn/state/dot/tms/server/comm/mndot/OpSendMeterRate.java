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
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to send release rate to a ramp meter.
 *
 * @author Douglas Lau
 */
public class OpSendMeterRate extends Op170Device {

	/** Ramp meter */
	private final RampMeterImpl meter;

	/** Red time (tenths of a second) or null for no metering */
	private final Integer red_time;

	/** Create a new send meter rate operation.
	 * @param rm Ramp meter.
	 * @param rate Release rate (vehicles / hour) or null to stop. */
	public OpSendMeterRate(RampMeterImpl rm, Integer rate) {
		super(PriorityLevel.COMMAND, rm);
		meter = rm;
		red_time = redTimeFromRate(rate);
	}

	/** Convert release rate to red time.
	 * @param rate release rate (vehicles / hour), or null.
	 * @return Red time (tenths of a second), or null. */
	private Integer redTimeFromRate(Integer rate) {
		return (rate != null)
		      ? RedTime.fromReleaseRate(rate, meter.getMeterType())
		      : null;
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object o) {
		if (o instanceof OpSendMeterRate) {
			OpSendMeterRate op = (OpSendMeterRate)o;
			return meter == op.meter && red_time == op.red_time;
		} else
			return false;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseTwo() {
		return (red_time != null) ? new SendRedTime() : new SendRate();
	}

	/** Phase to send the red time */
	protected class SendRedTime extends Phase<MndotProperty> {
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			MemoryProperty p = new MemoryProperty(redTimeAddress(),
				new byte[2]);
			p.formatBCD4(red_time);
			mess.add(p);
			mess.storeProps();
			return (meter.isMetering()) ? null : new SendRate();
		}
	}

	/** Get the red time address for the current timing table */
	private int redTimeAddress() {
		return redAddress(MeterRate.CENTRAL);
	}

	/** Phase to send the (remote) metering rate */
	protected class SendRate extends Phase<MndotProperty> {
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			MemoryProperty p = new MemoryProperty(
				remoteRateAddress(), new byte[1]);
			p.formatBCD2(remoteRate());
			mess.add(p);
			mess.storeProps();
			return null;
		}
	}

	/** Remote metering rate to send */
	private int remoteRate() {
		return (red_time != null)
		      ? MeterRate.CENTRAL
		      : MeterRate.FORCED_FLASH;
	}

	/** Get the controller address of the meter remote (central) rate */
	private int remoteRateAddress() {
		return meterAddress(Address.OFF_REMOTE_RATE);
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			meter.setRateNotify(releaseRate());
		super.cleanup();
	}

	/** Get the release rate (vehicles / hour) or null */
	private Integer releaseRate() {
		return (red_time != null) ? rateFromRedTime(red_time) : null;
	}

	/** Convert red time to release rate.
	 * @param rt Red time (tenths of a second).
	 * @return Release rate (vehicles / hour). */
	private int rateFromRedTime(int rt) {
		return RedTime.toReleaseRate(rt, meter.getMeterType());
	}
}
