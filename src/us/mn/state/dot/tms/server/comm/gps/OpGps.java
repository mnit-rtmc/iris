/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.gps;

import us.mn.state.dot.tms.server.GpsImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to be performed on a GPS modem.
 *
 * @author John L. Stanley
 */
abstract public class OpGps extends OpDevice<GpsProperty> {

	/** GPS modem to talk to */
	protected final GpsImpl gps;

	/** GPS property */
	protected final GpsProperty prop;
	
	/** Create a new GPS operation */
	public OpGps(PriorityLevel p, GpsImpl g, GpsProperty gprop) {
		super(p, g);
		gps = g;
		prop = gprop;
	}
}
