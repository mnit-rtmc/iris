/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.Controller170;
import us.mn.state.dot.tms.Controller170Impl;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.RampMeterImpl;
import us.mn.state.dot.tms.comm.ControllerOperation;

/**
 * 170 Controller operation
 *
 * @author Douglas Lau
 */
abstract public class Controller170Operation extends ControllerOperation {

	/** Lookup the first ramp meter on a 170 controller */
	static protected RampMeterImpl lookupMeter1(Controller170Impl c) {
		ControllerIO[] io_pins = c.getIO();
		ControllerIO io = io_pins[Controller170.DEVICE_1_PIN];
		if(io instanceof RampMeterImpl)
			return (RampMeterImpl)io;
		else
			return null;
	}

	/** Lookup the second ramp meter on a 170 controller */
	static protected RampMeterImpl lookupMeter2(Controller170Impl c) {
		ControllerIO[] io_pins = c.getIO();
		ControllerIO io = io_pins[Controller170.METER_2_PIN];
		if(io instanceof RampMeterImpl)
			return (RampMeterImpl)io;
		else
			return null;
	}

	/** 170 controller */
	protected final Controller170Impl c170;

	/** Ramp meter being queried */
	protected final RampMeterImpl meter1;

	/** Ramp meter being queried */
	protected final RampMeterImpl meter2;

	/** Create a new query meter status operatoin */
	public Controller170Operation(int p, Controller170Impl c) {
		super(p, c);
		c170 = c;
		meter1 = lookupMeter1(c170);
		meter2 = lookupMeter2(c170);
	}
}
