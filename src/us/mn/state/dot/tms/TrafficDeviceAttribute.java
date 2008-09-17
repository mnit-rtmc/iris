/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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

	/** attribute names common to all agencies */
	//String BLA_BLA_BLA = "bla_bla_bla";

	/** attribute names specific to D10 */
	String CAWS_CONTROLLED = "CAWS_controlled";

	/** Set the traffic device id */
	public void setId(String dms);

	/** Get the traffic device id */
	public String getId();

	/** Set the attribute name */
	public void setAttributeName(String aname);

	/** Get the attribute name */
	public String getAttributeName();

	/** Set the attribute value */
	public void setAttributeValue(String avalue);

	/** Get the attribute value */
	public String getAttributeValue();

	/** Set the attribute value as a boolean. */
	public void setAttributeValueBoolean(boolean arg);

	/** Get the attribute value as a boolean */
	public boolean getAttributeValueBoolean();

	/** Set the attribute value as an int */
	public void setAttributeValueInt(int arg);

	/** Get the attribute value as an int */
	public int getAttributeValueInt();

	/** toString */
	public String toString();
}

