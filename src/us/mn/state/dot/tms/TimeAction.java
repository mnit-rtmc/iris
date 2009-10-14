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

import us.mn.state.dot.sonar.SonarObject;

/**
 * Action for triggering an action plan to be deployed or undeployed.
 *
 * @author Douglas Lau
 */
public interface TimeAction extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "time_action";

	/** Get the action plan */
	ActionPlan getActionPlan();

	/** Get the day plan */
	DayPlan getDayPlan();

	/** Get the minute-of-day (0-1440) */
	short getMinute();

	/** Set the deploy trigger flag */
	void setDeploy(boolean d);

	/** Get the deploy trigger flag */
	boolean getDeploy();
}
