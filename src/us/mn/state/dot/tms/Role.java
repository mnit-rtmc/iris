/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2024  Minnesota Department of Transportation
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
 * A role is a set of permissions for user access.
 *
 * @author Douglas Lau
 */
public interface Role extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "role";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = Permission.SONAR_TYPE;

	/** Enable or disable the role */
	void setEnabled(boolean e);

	/** Get the enabled flag */
	boolean getEnabled();

	/** Set the allowed login domains */
	void setDomains(Domain[] d);

	/** Get the allowed login domains */
	Domain[] getDomains();
}
