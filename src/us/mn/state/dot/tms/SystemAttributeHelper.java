/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.Namespace;

/**
 * Static System Attribute convenience methods accessible from
 * the client and server. These methods are used to retrieve
 * system attributes and validate them.
 *
 * @author Michael Darter
 * @author Douglas Lau
 * @see SystemAttribute
 */
public class SystemAttributeHelper {

	/** SONAR namespace. For server code this is set in TMSImpl and
	 * for client code this is set in SonarState. */
	static public Namespace namespace;

	/** Disallow instantiation */
	protected SystemAttributeHelper() {
		assert false;
	}

	/** Get the SystemAttribute with the specified name.
	 * @param aname Name of an existing attribute.
	 * @return SystemAttribute or null if not found. */
	static public SystemAttribute get(String aname) {
		assert aname != null;
		assert aname.length() <= SystemAttribute.MAXLEN_ANAME;
		return lookup(aname);
	}

	/** Lookup a SystemAttribute in the SONAR namespace. 
	 *  @return The specified system attribute, or null if the it does not
	 *  exist in the namespace.
	 */
	static protected SystemAttribute lookup(String aname) {
		assert namespace != null;
		assert aname != null && aname.length() > 0;
		return (SystemAttribute)namespace.lookupObject(
			SystemAttribute.SONAR_TYPE, aname);
	}

	/** Get the meter minimum release rate (vehicles per hour) */
	static public int getMeterMinRelease() {
		float red = SystemAttrEnum.METER_MAX_RED_SECS.getFloat();
		return calculateReleaseRate(red);
	}

	/** Get the meter maximum release rate (vehicles per hour) */
	static public int getMeterMaxRelease() {
		float red = SystemAttrEnum.METER_MIN_RED_SECS.getFloat();
		return calculateReleaseRate(red);
	}

	/** Get the meter minimum release rate (vehicles per hour) */
	static protected int calculateReleaseRate(float red) {
		float green = SystemAttrEnum.METER_GREEN_SECS.getFloat();
		float yellow = SystemAttrEnum.METER_YELLOW_SECS.getFloat();
		float cycle = green + yellow + red;
		return (int)(Interval.HOUR / cycle);
	}
}
