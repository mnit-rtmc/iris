/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
 * A traffic device attribute is a name mapped to a string value. Device 
 * attributes are associated with a single traffic device, identified by id.
 * Attributes names specific to an agency should be defined in a subclass.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public interface TrafficDeviceAttribute extends SonarObject {

	/** maximum lengths of attributes */
	final int MAXLEN_NAME = 32;
	final int MAXLEN_ANAME = 32;

	/** SONAR type name */
	String SONAR_TYPE = "traffic_device_attribute";

	/** Set the traffic device id */
	void setId(String dms);

	/** Get the traffic device id */
	String getId();

	/** Set the attribute name */
	void setAName(String aname);

	/** Get the attribute name */
	String getAName();

	/** Set the attribute value */
	void setAValue(String avalue);

	/** Set the attribute value as boolean */
	//void setAValueBoolean(boolean aname);

	/** Get the attribute value */
	String getAValue();

	/** Get the attribute value as a boolean*/
	boolean getAValueBoolean();

	/** toString */
	String toString();

	/** DMS related attribute names */
	String AWS_CONTROLLED = "AWS_controlled";
}

