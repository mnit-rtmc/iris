/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
 * A system policy is a parameter name mapped to an integer value.
 *
 * @author Douglas Lau
 */
public interface SystemPolicy extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "system_policy";

	/** Meter green time parameter */
	String METER_GREEN_TIME = "meter_green_time";

	/** Meter yellow time parameter */
	String METER_YELLOW_TIME = "meter_yellow_time";

	/** Meter mimimum red time parameter */
	String METER_MIN_RED_TIME = "meter_min_red_time";

	/** DMS page on time parameter */
	String DMS_PAGE_ON_TIME = "dms_page_on_time";

	/** DMS page off time parameter */
	String DMS_PAGE_OFF_TIME = "dms_page_off_time";

	/** Incident ring radius 0 */
	String RING_RADIUS_0 = "ring_radius_0";

	/** Incident ring radius 1 */
	String RING_RADIUS_1 = "ring_radius_1";

	/** Incident ring radius 2 */
	String RING_RADIUS_2 = "ring_radius_2";

	/** Incident ring radius 3 */
	String RING_RADIUS_3 = "ring_radius_3";

	/** Set a new value of a system-wide policy parameter */
	void setValue(int value);

	/** Get the current value of a system-wide policy parameter */
	int getValue();
}
