/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
 * Timing plan
 *
 * @author Douglas Lau
 */
public interface TimingPlan extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "timing_plan";

	/** Get the plan type */
	int getPlanType();

	/** Get the device */
	Device getDevice();

	/** Set the start time (minute of day) */
	void setStartMin(int t);

	/** Get the start time (minute of day) */
	int getStartMin();

	/** Set the stop time (minute of day) */
	void setStopMin(int t);

	/** Get the stop time (minute of day) */
	int getStopMin();

	/** Set the active status */
	void setActive(boolean a);

	/** Get the active status */
	boolean getActive();

	/** Set the testing status */
	void setTesting(boolean t);

	/** Get the testing status */
	boolean getTesting();

	/** Set the target value */
	void setTarget(int t);

	/** Get the target value */
	int getTarget();
}
