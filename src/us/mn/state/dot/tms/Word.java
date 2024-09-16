/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Iteris Inc.
 * Copyright (C) 2020-2024  Minnesota Department of Transportation
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
 * A word which can be banned or allowed in free-form DMS messages.
 *
 * @author Michael Darter
 */
public interface Word extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "word";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** Get the abbreviation */
	String getAbbr();

	/** Set the abbreviation */
	void setAbbr(String abbr);

	/** Get the type: allowed or banned */
	boolean getAllowed();

	/** Set the type: allowed or banned */
	void setAllowed(boolean allowed);
}
