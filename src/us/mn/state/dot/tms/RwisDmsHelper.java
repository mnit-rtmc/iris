/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  SRF Consulting Group
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
 * Helper class for RwisSign objects.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class RwisDmsHelper extends BaseHelper {
	
	/** Don't allow instances to be created */
	private RwisDmsHelper() {
		assert false;
	}

	/** Lookup the RwisCondition with the specified DMS name */
	static public RwisSign lookup(String name) {
		return (RwisSign)namespace.lookupObject(RwisSign.SONAR_TYPE,
				name);
	}

	/** Get an RwisCondition iterator */
	static public Iterator<RwisSign> iterator() {
		return new IteratorWrapper<RwisSign>(namespace.iterator(
				RwisSign.SONAR_TYPE));
	}
}
