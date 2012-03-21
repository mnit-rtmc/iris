/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.g4;

import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * G4 Statistical Property
 *
 * @author Michael Darter
 */
public class StatProperty extends G4Property {

	/** Create a new binned sample property.
	 * @param sid Sensor id */
	public StatProperty(ControllerImpl c, G4Rec r) {
		super(c, r);
	}

	/** Format a data request */
	protected G4Blob formatGetRequest() {
		return G4Blob.buildDataRequest(sensor_id);
	}
}
