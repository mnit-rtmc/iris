/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2018  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.GroupChecker;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.SonarObject;

/**
 * An action plan is a set of actions which can be deployed together.
 *
 * @author Douglas Lau
 */
public interface ActionPlan extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "action_plan";

	/** Set the description */
	void setDescription(String d);

	/** Get the description */
	String getDescription();

	/** Set the group name */
	void setGroupN(String g);

	/** Get the group name */
	String getGroupN();

	/** Set the sync actions flag */
	void setSyncActions(boolean s);

	/** Get the sync actions flag */
	boolean getSyncActions();

	/** Set the sticky flag */
	void setSticky(boolean s);

	/** Get the sticky flag */
	boolean getSticky();

	/** Set the active status */
	void setActive(boolean a);

	/** Get the active status */
	boolean getActive();

	/** Set the default phase */
	void setDefaultPhase(PlanPhase dp);

	/** Get the default phase */
	PlanPhase getDefaultPhase();

	/** Set the phase */
	void setPhase(PlanPhase p);

	/** Get the phase */
	PlanPhase getPhase();

	/** Group privilege checker */
	GroupChecker GROUP_CHECKER = new GroupChecker() {
		public boolean checkGroup(Name name, User u, String g) {
			String n = name.getObjectPart();
			ActionPlan ap = ActionPlanHelper.lookup(n);
			return ap != null && g.equals(ap.getGroupN());
		}
	};
}
