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
package us.mn.state.dot.tms;

import us.mn.state.dot.sonar.Checker;

/**
 * Helper class for Incident.
 *
 * @author Douglas Lau
 */
public class IncidentHelper extends BaseHelper {

	/** don't instantiate */
	private IncidentHelper() {
		assert false;
	}

	/** Lookup the Incident with the specified name */
	static public Incident lookup(String name) {
		return (Incident)namespace.lookupObject(Incident.SONAR_TYPE,
			name);
	}

	/** Find Incident using a Checker */
	static public Incident find(final Checker<Incident> checker) {
		return (Incident)namespace.findObject(Incident.SONAR_TYPE,
			checker);
	}

	/** Lookup the camera for an incident */
	static public Camera getCamera(Incident inc) {
		if(inc != null)
			return inc.getCamera();
		else
			return null;
	}
}
