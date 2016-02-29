/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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

/**
 * Helper class for incident details.
 *
 * @author Douglas Lau
 */
public class IncidentDetailHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private IncidentDetailHelper() {
		assert false;
	}

	/** Lookup incident detail with the specified name */
	static public IncidentDetail lookup(String name) {
		return (IncidentDetail) namespace.lookupObject(
			IncidentDetail.SONAR_TYPE, name);
	}
}
