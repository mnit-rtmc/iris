/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to set the red time for a ramp meter
 *
 * @author Douglas Lau
 */
public class OpSendMeterRedTime extends OpDevice {

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
		int a = Address.METER_1_TIMING_TABLE;
		if(m == 2)
			a = Address.METER_2_TIMING_TABLE;
		if(MndotPoller.isAfternoon())
			a += Address.OFF_PM_TIMING_TABLE;
		a += Address.OFF_RED_TIME;
		a += MeterRate.CENTRAL * 2;
		address = a;
		red_time = red;
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return new SetRedTime();
	}

	/** Phase to set the red time for a ramp meter */
	protected class SetRedTime extends Phase {

		/** Write the new red time to the controller */
		protected Phase poll(CommMessage mess) throws IOException {
			ByteArrayOutputStream bo = new ByteArrayOutputStream(2);
			BCDOutputStream os = new BCDOutputStream(bo);
			os.write4(red_time);
			mess.add(new MemoryProperty(address, bo.toByteArray()));
			mess.storeProps();
			float red = red_time / 10.0f;
			int rate = MndotPoller.calculateReleaseRate(meter, red);
			// FIXME: should happen on SONAR thread
			meter.setRateNotify(rate);
			return null;
		}
	}
}
