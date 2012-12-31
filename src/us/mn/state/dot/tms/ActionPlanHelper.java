/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
 * Helper class for action plans.
 *
 * @author Douglas Lau
 */
public class ActionPlanHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private ActionPlanHelper() {
		assert false;
	}

	/** Lookup the action plan with the specified name */
	static public ActionPlan lookup(String name) {
		return (ActionPlan)namespace.lookupObject(ActionPlan.SONAR_TYPE,
			name);
	}

	/** Get an action plan iterator */
	static public Iterator<ActionPlan> iterator() {
		return new IteratorWrapper<ActionPlan>(namespace.iterator(
			ActionPlan.SONAR_TYPE));
	}
}
