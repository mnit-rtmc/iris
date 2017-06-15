/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017 Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation performed on an ESS
 *
 * @author Michael Darter
 */
abstract public class OpEss extends OpNtcip {

	/** Field sensor */
	protected final WeatherSensorImpl w_sensor; 

	/** Constructor */
	public OpEss(PriorityLevel p, WeatherSensorImpl ws) {
		super(p, ws);
		w_sensor = ws;
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		super.cleanup();
	}
}
