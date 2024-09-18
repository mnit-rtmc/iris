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
 * A user account which can access IRIS.
 *
 * @author Douglas Lau
 */
public interface User extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "user_id";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = Permission.SONAR_TYPE;

	/** Set the user's full name */
	void setFullName(String n);

	/** Get the user's full name */
	String getFullName();

	/** Set the password */
	void setPassword(String pwd);

	/** Set the LDAP Distinguished Name */
	void setDn(String d);

	/** Get the LDAP Distinguished Name */
	String getDn();

	/** Set the role */
	void setRole(Role r);

	/** Get the role */
	Role getRole();

	/** Set the enabled flag */
	void setEnabled(boolean e);

	/** Get the enabled flag */
	boolean getEnabled();
}
