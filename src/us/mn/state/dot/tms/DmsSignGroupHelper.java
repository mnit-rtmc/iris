/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
 * Helper class for DMS sign groups.
 *
 * @author Douglas Lau
 */
public class DmsSignGroupHelper extends BaseHelper {

	/** Prevent object creation */
	private DmsSignGroupHelper() {
		assert false;
	}

	/** Get a DMS sign group iterator */
	static public Iterator<DmsSignGroup> iterator() {
		return new IteratorWrapper<DmsSignGroup>(namespace.iterator(
			DmsSignGroup.SONAR_TYPE));
	}
}
