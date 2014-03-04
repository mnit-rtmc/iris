/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
 * Helper class for beacon actions.
 *
 * @author Douglas Lau
 */
public class BeaconActionHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private BeaconActionHelper() {
		assert false;
	}

	/** Lookup the beacon action with the specified name */
	static public BeaconAction lookup(String name) {
		return (BeaconAction)namespace.lookupObject(
			BeaconAction.SONAR_TYPE, name);
	}

	/** Get a beacon action iterator */
	static public Iterator<BeaconAction> iterator() {
		return new IteratorWrapper<BeaconAction>(namespace.iterator(
			BeaconAction.SONAR_TYPE));
	}
}
