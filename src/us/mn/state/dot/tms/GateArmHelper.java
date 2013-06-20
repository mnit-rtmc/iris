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
 * Helper class for gate arms.  Used on the client and server.
 *
 * @author Douglas Lau
 */
public class GateArmHelper extends BaseHelper {

	/** don't instantiate */
	private GateArmHelper() {
		assert false;
	}

	/** Lookup the gate arm with the specified name */
	static public GateArm lookup(String name) {
		return (GateArm)namespace.lookupObject(GateArm.SONAR_TYPE,name);
	}

	/** Get a gate arm iterator */
	static public Iterator<GateArm> iterator() {
		return new IteratorWrapper<GateArm>(namespace.iterator(
			GateArm.SONAR_TYPE));
	}
}
