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

/**
 * Helper class for plan phases.
 *
 * @author Douglas Lau
 */
public class PlanPhaseHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private PlanPhaseHelper() {
		assert false;
	}

	/** Lookup the phase with the specified name */
	static public PlanPhase lookup(String name) {
		return (PlanPhase)namespace.lookupObject(PlanPhase.SONAR_TYPE,
			name);
	}
}
