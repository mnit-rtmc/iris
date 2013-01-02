/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2012  Minnesota Department of Transportation
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
 * Helper class for meter actions.
 *
 * @author Douglas Lau
 */
public class MeterActionHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private MeterActionHelper() {
		assert false;
	}

	/** Lookup the meter action with the specified name */
	static public MeterAction lookup(String name) {
		return (MeterAction)namespace.lookupObject(
			MeterAction.SONAR_TYPE, name);
	}

	/** Get a meter action iterator */
	static public Iterator<MeterAction> iterator() {
		return new IteratorWrapper<MeterAction>(namespace.iterator(
			MeterAction.SONAR_TYPE));
	}
}
