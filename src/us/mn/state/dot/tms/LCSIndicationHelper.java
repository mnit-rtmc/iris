/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
 * Helper for LCS indications.
 *
 * @author Douglas Lau
 */
public class LCSIndicationHelper extends BaseHelper {

	/** Disallow instantiation */
	private LCSIndicationHelper() {
		assert false;
	}

	/** Get an LCS indication iterator */
	static public Iterator<LCSIndication> iterator() {
		return new IteratorWrapper<LCSIndication>(namespace.iterator(
			LCSIndication.SONAR_TYPE));
	}
}
