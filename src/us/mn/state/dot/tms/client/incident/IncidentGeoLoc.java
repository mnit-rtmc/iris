/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;

/**
 * Map object for incidents.
 *
 * @author Douglas Lau
 */
public class IncidentGeoLoc extends MapGeoLoc {

	/** Incident associated with the map object */
	protected final Incident incident;

	/** Get the incident associated with the map object */
	public Incident getIncident() {
		return incident;
	}

	/** Create a new incident geo loc */
	public IncidentGeoLoc(Incident inc, GeoLoc loc) {
		super(loc);
		incident = inc;
	}
}
