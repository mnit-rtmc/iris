/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.RampMeterType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * 170 Controller operation
 *
 * @author Douglas Lau
 */
abstract public class Op170 extends OpController<MndotProperty> {

	/** I/O pin for first traffic device */
	static protected final int DEVICE_1_PIN = 2;

	/** I/O pin for second ramp meter */
	static protected final int METER_2_PIN = 3;

	/** I/O pin for the first special function output */
	static public final int SPECIAL_FUNCTION_OUTPUT_PIN = 19;

	/** I/O pin for the first detector */
	static protected final int FIRST_DETECTOR_PIN = 39;

	/** Total number of detector inputs on a 170 controller */
	static protected final int DETECTOR_INPUTS = 24;

	/** Special function input pin for first alarm */
	static protected final int ALARM_PIN = 70;

	/** Test if a pin is set in the special function output buffer */
	static public boolean getSpecFuncOutput(byte[] buffer, int pin) {
		int i = pin - SPECIAL_FUNCTION_OUTPUT_PIN;
		if(i >= 0 && i < 8)
			return ((buffer[0] >> i) & 1) != 0;
		i -= 8;
		if(i >= 0 && i < 8)
			return ((buffer[1] >> i) & 1) != 0;
		return false;
	}

	/** Set the specified pin in a special function output buffer */
	static public void setSpecFuncOutput(byte[] buffer, int pin) {
		int i = pin - SPECIAL_FUNCTION_OUTPUT_PIN;
		if(i >= 0 && i < 8)
			buffer[0] |= 1 << i;
		i -= 8;
		if(i >= 0 && i < 8)
			buffer[1] |= 1 << i;
	}

	/** Clear the specified pin in a special function output buffer */
	static public void clearSpecFuncOutput(byte[] buffer, int pin) {
		int i = pin - SPECIAL_FUNCTION_OUTPUT_PIN;
		if(i >= 0 && i < 8)
			buffer[0] &= (1 << i) ^ 0xFF;
		i -= 8;
		if(i >= 0 && i < 8)
			buffer[1] &= (1 << i) ^ 0xFF;
	}

	/** Lookup the first ramp meter on a 170 controller */
	static public RampMeterImpl lookupMeter1(ControllerImpl c) {
		ControllerIO io = c.getIO(DEVICE_1_PIN);
		if(io instanceof RampMeterImpl)
			return (RampMeterImpl)io;
		else
			return null;
	}

	/** Lookup the second ramp meter on a 170 controller */
	static public RampMeterImpl lookupMeter2(ControllerImpl c) {
		ControllerIO io = c.getIO(METER_2_PIN);
		if(io instanceof RampMeterImpl)
			return (RampMeterImpl)io;
		else
			return null;
	}

	/** Adjust the green count for single release meters */
	static protected int adjustGreenCount(RampMeterImpl meter, int g) {
		if(meter.getMeterType() == RampMeterType.SINGLE.ordinal()) {
			if((g % 2) != 0)
				g++;
			return g / 2;
		} else
			return g;
	}

	/** Create a new 170 operation */
	protected Op170(PriorityLevel p, ControllerImpl c) {
		super(p, c);
	}
}
