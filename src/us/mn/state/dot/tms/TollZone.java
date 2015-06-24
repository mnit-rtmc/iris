/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
 * A toll zone is a roadway segment which is tolled by usage.
 *
 * @author Douglas Lau
 */
public interface TollZone extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "toll_zone";

	/** Set the starting station ID */
	void setStartID(String sid);

	/** Get the starting station ID */
	String getStartID();

	/** Set the ending station ID */
	void setEndID(String sid);

	/** Get the ending station ID */
	String getEndID();
}
