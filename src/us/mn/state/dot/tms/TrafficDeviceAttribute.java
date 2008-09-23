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
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public interface TrafficDeviceAttribute extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "traffic_device_attribute";

	/** attribute names specific to D10 */
	String CAWS_CONTROLLED = "CAWS_controlled";

	/** Set the traffic device id */
	void setId(String dms);

	/** Get the traffic device id */
	String getId();

	/** Set the attribute name */
	void setAttributeName(String aname);

	/** Get the attribute name */
	String getAttributeName();

	/** Set the attribute value */
	void setAttributeValue(String avalue);

	/** Get the attribute value */
	String getAttributeValue();
}
