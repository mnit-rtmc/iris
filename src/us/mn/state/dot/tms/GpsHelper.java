/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
 * Copyright (C) 2018-2024  Minnesota Department of Transportation
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
 * Helper class for Gps.  Used on the client and server.
 *
 * @author John L. Stanley - SRF Consulting
 * @author Douglas Lau
 */
public class GpsHelper extends BaseHelper {

	/** don't instantiate */
	private GpsHelper() {
		assert false;
	}

	/** Lookup the Gps with the specified name */
	static public Gps lookup(String name) {
		return (Gps) namespace.lookupObject(Gps.SONAR_TYPE, name);
	}

	/** Get a Gps iterator */
	static public Iterator<Gps> iterator() {
		return new IteratorWrapper<Gps>(namespace.iterator(
			Gps.SONAR_TYPE));
	}

	/** Find Gps for a geo location */
	static public Gps findLoc(GeoLoc loc) {
		if (loc != null) {
			Iterator<Gps> it = iterator();
			while (it.hasNext()) {
				Gps gps = it.next();
				if (gps.getGeoLoc() == loc)
					return gps;
			}
		}
		return null;
	}
}
