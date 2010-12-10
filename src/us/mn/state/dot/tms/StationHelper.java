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
 * Helper class for stations.
 *
 * @author Douglas Lau
 */
public class StationHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private StationHelper() {
		assert false;
	}

	/** Find stations using a Checker */
	static public Station find(Checker<Station> checker) {
		return (Station)namespace.findObject(Station.SONAR_TYPE,
			checker);
	}

	/** Lookup the station with the specified name */
	static public Station lookup(String name) {
		return (Station)namespace.lookupObject(Station.SONAR_TYPE,
			name);
	}

	/** Get the station label */
	static public String getLabel(Station s) {
		return GeoLocHelper.getRootLabel(s.getR_Node().getGeoLoc());
	}
}
