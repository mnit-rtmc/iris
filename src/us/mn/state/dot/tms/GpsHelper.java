/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
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
 *
 * (Derived from DmsHelper.java.)
 */
package us.mn.state.dot.tms;

import java.util.Iterator;
import us.mn.state.dot.tms.utils.SString;

/**
 * Helper class for Gps. Used on the client and server.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class GpsHelper extends BaseHelper {

	/** don't instantiate */
	private GpsHelper() {
		assert false;
	}

	/** Lookup the Gps with the specified name */
	static public Gps lookup(String name) {
		return (Gps)namespace.lookupObject(Gps.SONAR_TYPE, name);
	}

	/** Get a Gps iterator */
	static public Iterator<Gps> iterator() {
		return new IteratorWrapper<Gps>(namespace.iterator(
			Gps.SONAR_TYPE));
	}
}
