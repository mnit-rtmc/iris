/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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
 * Permissions control a user's access to resources.
 *
 * @author Douglas Lau
 */
public interface Permission extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "permission";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** Get the role */
	Role getRole();

	/** Get the base resource */
	String getBaseResource();

	/** Get hashtag */
	String getHashtag();

	/** Set hashtag */
	void setHashtag(String h);

	/** Get the access level */
	int getAccessLevel();

	/** Set the access level */
	void setAccessLevel(int a);
}
