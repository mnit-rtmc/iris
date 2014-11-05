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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query the status of a ramp meter.
 *
 * @author Douglas Lau
 */
public class OpQueryMeterStatus extends Op170Device {

	/** Police panel bit from verify data from 170 */
	static private final int POLICE_PANEL_BIT = 1 << 4;

	/** Ramp meter */
	private final RampMeterImpl meter;

	/** Data buffer */
	private final byte[] data = new byte[5];

	/** Release rate (vehicles / hour) */
	private Integer rate;

	/** Create a new query meter status operation.
	 * @param rm Ramp meter. */
	public OpQueryMeterStatus(RampMeterImpl rm) {
		super(PriorityLevel.DATA_30_SEC, rm);
		meter = rm;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseTwo() {
		return new QueryMeterData();
	}

	/** Phase to query the meter data */
	protected class QueryMeterData extends Phase<MndotProperty> {
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			MemoryProperty data_mem = new MemoryProperty(
				meterAddress(Address.OFF_STATUS), data);
			mess.add(data_mem);
			mess.queryProps();
			parseMeterData();
			return isRateMetering() ? new QueryRedTime() : null;
		}
	}

	/** Parse meter data */
	private void parseMeterData() throws IOException {
		validateMeterState();
		updateMeterLocks();
		updateGreenCount();
	}

	/** Validate meter status and current rate */
	private void validateMeterState() throws InvalidStateException {
		int s = data[Address.OFF_STATUS];
		int r = currentRate();
		if (!MeterStatus.isValid(s) ||
		    !MeterRate.isValid(r) ||
		    MeterStatus.isMetering(s) != MeterRate.isMetering(r))
			throw new InvalidStateException(s, r);
	}

	/** Get the current metering rate */
	private int currentRate() {
		return data[Address.OFF_CURRENT_RATE];
	}

	/** Update ramp meter locks */
	private void updateMeterLocks() {
		meter.setPolicePanel(isPolicePanelOn());
		int s = data[Address.OFF_STATUS];
		meter.setManual(MeterStatus.isManual(s));
	}

	/** Is the police panel switch on? */
	private boolean isPolicePanelOn() {
		int p = data[Address.OFF_POLICE_PANEL];
		return (p & POLICE_PANEL_BIT) != 0;
	}

	/** Update the green count */
	private void updateGreenCount() {
		int g = data[Address.OFF_GREEN_COUNT_30];
		meter.updateGreenCount(TimeSteward.currentTimeMillis(),
			Op170.adjustGreenCount(meter, g));
	}

	/** Check if current rate is metering (not flashing). */
	private boolean isRateMetering() {
		return MeterRate.isMetering(currentRate());
	}

	/** Phase to query a ramp meter red time */
	protected class QueryRedTime extends Phase<MndotProperty> {
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			MemoryProperty red_mem = new MemoryProperty(
				redTimeAddress(), new byte[2]);
			mess.add(red_mem);
			mess.queryProps();
			rate = RedTime.toReleaseRate(red_mem.parseBCD4(),
				meter.getMeterType());
			return null;
		}
	}

	/** Get the red time address for the current metering rate */
	private int redTimeAddress() {
		return redAddress(currentRate());
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			meter.setRateNotify(rate);
		super.cleanup();
	}
}
