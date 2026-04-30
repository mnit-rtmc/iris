/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026  Minnesota Department of Transportation
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
 * Action for triggering an action plan to change phases.
 *
 * @author Douglas Lau
 */
public interface PhaseAction extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "phase_action";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = ActionPlan.SONAR_TYPE;

	/** Get the action plan */
	ActionPlan getActionPlan();

	/** Set the day plan */
	void setDayPlan(DayPlan dp);

	/** Get the day plan */
	DayPlan getDayPlan();

	/** Set the "from" phase */
	void setFromPhase(PlanPhase fp);

	/** Get the "from" phase */
	PlanPhase getFromPhase();

	/** Set the "to" phase */
	void setToPhase(PlanPhase tp);

	/** Get the "to" phase */
	PlanPhase getToPhase();

	/** Set the action condition */
	void setCondition(int c);

	/** Get the action condition */
	int getCondition();

	/** Set the condition parameters */
	void setParams(String p);

	/** Get the condition parameters */
	String getParams();
}
