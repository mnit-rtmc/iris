/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;

/**
 * Base helper class for client/server interfaces.
 *
 * @author Douglas Lau
 */
abstract public class BaseHelper {

	/** Compare two (possibly-null) objects for equality */
	static protected boolean objectEquals(Object o0, Object o1) {
		return (o0 != null) ? o0.equals(o1) : o1 == null;
	}

	/** SONAR namespace. For server code this is set in TMSImpl and
	 * for client code this is set in SonarState. */
	static public Namespace namespace;

	/** SONAR user.  For server code this is null. */
	static public User user;

	/** Prevent object creation */
	protected BaseHelper() {
		assert false;
	}

	/** Check if a type can be read */
	static protected boolean canRead(String tname) {
		return (user != null)
		    && namespace.canRead(new Name(tname), user);
	}
}
