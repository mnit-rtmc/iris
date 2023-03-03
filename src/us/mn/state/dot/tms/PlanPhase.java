/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2021  Minnesota Department of Transportation
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
 * Phase for action plans.
 *
 * @author Douglas Lau
 */
public interface PlanPhase extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "plan_phase";

	/** Undeployed phase name */
	String UNDEPLOYED = "undeployed";

	/** Alert before phase name */
	String ALERT_BEFORE = "alert_before";

	/** Alert during phase name */
	String ALERT_DURING = "alert_during";

	/** Alert after phase name */
	String ALERT_AFTER = "alert_after";

	/** Gate arm open phase name */
	String GATE_ARM_OPEN = "ga_open";

	/** Gate arm closed phase name */
	String GATE_ARM_CLOSED = "ga_closed";

	/** Set the hold time (seconds) */
	void setHoldTime(int s);

	/** Get the hold time (seconds) */
	int getHoldTime();

	/** Set the next phase */
	void setNextPhase(PlanPhase np);

	/** Get the next phase */
	PlanPhase getNextPhase();
}
