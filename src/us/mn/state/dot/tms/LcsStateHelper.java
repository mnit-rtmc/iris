/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
 * Helper for LCS states.
 *
 * @author Douglas Lau
 */
public class LcsStateHelper extends BaseHelper {

	/** Disallow instantiation */
	private LcsStateHelper() {
		assert false;
	}

	/** Get an LCS state iterator */
	static public Iterator<LcsState> iterator() {
		return new IteratorWrapper<LcsState>(namespace.iterator(
			LcsState.SONAR_TYPE));
	}
}
