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
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to set the red time for a ramp meter
 *
 * @author Douglas Lau
 */
public class OpSendMeterRedTime extends Op170Device {

	/** Ramp meter */
	protected final RampMeterImpl meter;

	/** Memory address to write red time */
	protected final int address;

	/** Red time (in tenths of a second) */
	protected final int red_time;

	/** Create a set red time packet */
	public OpSendMeterRedTime(RampMeterImpl r, int m, int red) {
		super(PriorityLevel.COMMAND, r);
		meter = r;
		address = Op170.getRedAddress(m, MeterRate.CENTRAL);
		red_time = red;
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object o) {
		if (o instanceof OpSendMeterRedTime) {
			OpSendMeterRedTime op = (OpSendMeterRedTime)o;
			return meter == op.meter && red_time == op.red_time;
		} else
			return false;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseTwo() {
		return new SetRedTime();
	}

	/** Phase to set the red time for a ramp meter */
	protected class SetRedTime extends Phase<MndotProperty> {

		/** Write the new red time to the controller */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			ByteArrayOutputStream bo = new ByteArrayOutputStream(2);
			BCDOutputStream os = new BCDOutputStream(bo);
			os.write4(red_time);
			mess.add(new MemoryProperty(address, bo.toByteArray()));
			mess.storeProps();
			float red = red_time / 10.0f;
			int rate = RedTime.toReleaseRate(red,
				meter.getMeterType());
			// FIXME: should happen on SONAR thread
			meter.setRateNotify(rate);
			return null;
		}
	}
}
