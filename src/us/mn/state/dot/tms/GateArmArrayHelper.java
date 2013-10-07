/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
 * Helper class for gate arm arrays.  Used on the client and server.
 *
 * @author Douglas Lau
 */
public class GateArmArrayHelper extends BaseHelper {

	/** don't instantiate */
	private GateArmArrayHelper() {
		assert false;
	}

	/** Lookup the gate arm array with the specified name */
	static public GateArmArray lookup(String name) {
		return (GateArmArray)namespace.lookupObject(
			GateArmArray.SONAR_TYPE, name);
	}

	/** Get a gate arm array iterator */
	static public Iterator<GateArmArray> iterator() {
		return new IteratorWrapper<GateArmArray>(namespace.iterator(
			GateArmArray.SONAR_TYPE));
	}

	/** Check if a DMS is associated with a gate arm array */
	static public boolean checkDMS(DMS dms) {
		Iterator<GateArmArray> it = iterator();
		while(it.hasNext()) {
			GateArmArray ga = it.next();
			if(dms == ga.getDms())
				return true;
		}
		return false;
	}
}
