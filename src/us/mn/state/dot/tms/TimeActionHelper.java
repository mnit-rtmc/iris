/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.Checker;

/**
 * Helper class for time actions.
 *
 * @author Douglas Lau
 */
public class TimeActionHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private TimeActionHelper() {
		assert false;
	}

	/** Find time actions using a Checker */
	static public TimeAction find(final Checker<TimeAction> checker) {
		return (TimeAction)namespace.findObject(TimeAction.SONAR_TYPE, 
			checker);
	}

	/** Lookup the time action with the specified name */
	static public TimeAction lookup(String name) {
		return (TimeAction)namespace.lookupObject(TimeAction.SONAR_TYPE,
			name);
	}
}
