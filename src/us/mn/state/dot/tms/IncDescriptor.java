/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2025  Minnesota Department of Transportation
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
 * An incident descriptor is part of a message to deploy on a DMS, matching
 * incident attributes.
 *
 * @author Douglas Lau
 */
public interface IncDescriptor extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "inc_descriptor";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = Incident.SONAR_TYPE;

	/** Set the event type */
	void setEventType(int et);

	/** Get the event type */
	int getEventType();

	/** Set the incident detail */
	void setDetail(IncDetail dtl);

	/** Get the incident detail */
	IncDetail getDetail();

	/** Set the lane code */
	void setLaneCode(String lc);

	/** Get the lane code */
	String getLaneCode();

	/** Set the MULTI string */
	void setMulti(String m);

	/** Get the MULTI string */
	String getMulti();
}
