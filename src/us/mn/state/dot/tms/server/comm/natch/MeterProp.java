/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.natch;

import us.mn.state.dot.tms.RampMeterType;
import us.mn.state.dot.tms.server.RampMeterImpl;

/**
 * Meter property
 *
 * @author Douglas Lau
 */
abstract public class MeterProp extends NatchProp {

	/** Ramp Meter */
	protected final RampMeterImpl meter;

	/** Get the meter number */
	protected int getMeterNumber() {
		switch (meter.getPin()) {
		case 2: return 0;
		case 3: return 1;
		default: return 2;
		}
	}

	/** Create a new meter property */
	protected MeterProp(Counter c, RampMeterImpl m) {
		super(c);
		meter = m;
	}
}
