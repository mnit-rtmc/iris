/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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

/**
 * User helper methods.
 *
 * @author Michael Darter
 * @author Douglas Lau
 * @see User
 */
public class UserHelper extends BaseHelper {

	/** Disallow instantiation */
	protected UserHelper() {
		assert false;
	}

	/** Lookup a User in the SONAR namespace. 
	 *  @return The specified user or null if it does not exist. */
	static public User lookup(String name) {
		return (User) namespace.lookupObject(User.SONAR_TYPE, name);
	}
}
