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
 * A Lane-Use Control Signal is a special DMS which is designed to display
 * lane-use indications.
 *
 * @author Douglas Lau
 */
public interface LCS extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "lcs";

	/** Get the LCS array */
	LCSArray getArray();

	/** Get the lane number (starting from right lane as 1) */
	int getLane();
}
