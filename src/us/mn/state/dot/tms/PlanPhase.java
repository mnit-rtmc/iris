/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2025  Minnesota Department of Transportation
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

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = ActionPlan.SONAR_TYPE;

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

	/** Gate arm warn close phase name */
	String GATE_ARM_WARN_CLS = "ga_warn_cls";

	/** Gate arm closed phase name */
	String GATE_ARM_CLOSED = "ga_closed";

	/** Set selectable flag */
	void setSelectable(boolean s);

	/** Get selectable flag */
	boolean getSelectable();

	/** Set hold time (s) before next phase */
	void setHoldTime(Integer s);

	/** Get hold time (s) before next phase */
	Integer getHoldTime();

	/** Set next phase after hold time expires */
	void setNextPhase(PlanPhase np);

	/** Get next phase after hold time expires */
	PlanPhase getNextPhase();
}
