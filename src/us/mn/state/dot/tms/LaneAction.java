/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.SonarObject;

/**
 * Action for deploying a lane marking triggered by an action plan.
 *
 * @author Douglas Lau
 */
public interface LaneAction extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "lane_action";

	/** Get the action plan */
	ActionPlan getActionPlan();

	/** Get the lane marking */
	LaneMarking getLaneMarking();

	/** Set the phase to perform action */
	void setPhase(PlanPhase p);

	/** Get the phase to perform action */
	PlanPhase getPhase();
}
