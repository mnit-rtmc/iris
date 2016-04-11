/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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

import java.util.Iterator;

/**
 * Helper class for Incident.
 *
 * @author Douglas Lau
 */
public class IncidentHelper extends BaseHelper {

	/** Don't instantiate */
	private IncidentHelper() {
		assert false;
	}

	/** Lookup the Incident with the specified name */
	static public Incident lookup(String name) {
		return (Incident) namespace.lookupObject(Incident.SONAR_TYPE,
			name);
	}

	/** Get an incident iterator */
	static public Iterator<Incident> iterator() {
		return new IteratorWrapper<Incident>(namespace.iterator(
			Incident.SONAR_TYPE));
	}

	/** Lookup the camera for an incident */
	static public Camera getCamera(Incident inc) {
		return (inc != null) ? inc.getCamera() : null;
	}

	/** Get the severity of an incident */
	static public IncSeverity getSeverity(Incident inc) {
		IncidentImpact[] imp = IncidentImpact.fromString(
			inc.getImpact());
		if (isMajor(imp))
			return IncSeverity.major;
		if (isNormal(imp))
			return IncSeverity.normal;
		if (isMinor(imp))
			return IncSeverity.minor;
		else
			return null;
	}

	/** Check if the severity is major (all lanes blocked) */
	static private boolean isMajor(IncidentImpact[] imp) {
		for (int i = 1; i < imp.length - 1; i++) {
			if (imp[i] != IncidentImpact.BLOCKED)
				return false;
		}
		return imp.length > 2;
	}

	/** Check if the severity is normal (lane blocked) */
	static private boolean isNormal(IncidentImpact[] imp) {
		for (int i = 1; i < imp.length - 1; i++) {
			if (imp[i] == IncidentImpact.BLOCKED)
				return true;
		}
		return false;
	}

	/** Check if the severity is minor */
	static private boolean isMinor(IncidentImpact[] imp) {
		for (int i = 0; i < imp.length; i++) {
			if (imp[i] != IncidentImpact.FREE_FLOWING)
				return true;
		}
		return false;
	}
}
