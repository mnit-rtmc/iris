/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
 * An incident is an event (crash, stall, etc.) which has an effect on traffic.
 *
 * @author Douglas Lau
 */
public interface Incident extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "incident";

	/** Get name of incident this replaces */
	String getReplaces();

	/** Get the event type */
	int getEventType();

	/** Get the incident detail */
	IncidentDetail getDetail();

	/** Get the event date (timestamp) */
	long getEventDate();

	/** Get the lane type ordinal */
	short getLaneType();

	/** Get the road */
	Road getRoad();

	/** Get the road direction */
	short getDir();

	/** Get the latitude */
	double getLat();

	/** Get the longitude */
	double getLon();

	/** Get the verification camera */
	Camera getCamera();

	/** Get the current impact code.
	 * This is a coded string which indicates the lanes impacted by the
	 * incident.
	 * Each character in the string represents one lane (or shoulder) of
	 * the road at the incident location.
	 * The first character indicates the condition of the left shoulder.
	 * The second character represents the leftmost lane.
	 * Each subsequent character represents the next lane to the right,
	 * until the right lane.
	 * The last character represents the condition of the right shoulder.
	 * There are three characters to indicate the status for each lane:
	 * <pre>
	 *     .  Free-flowing (no obstruction)
	 *     ?  Partially blocked (debris, etc.)
	 *     !  Completely blocked
	 * </pre>
	 * @see us.mn.state.dot.tms.IncidentImpact */
	String getImpact();

	/** Set the impact code */
	void setImpact(String imp);

	/** Get the cleared status */
	boolean getCleared();

	/** Set the cleared status */
	void setCleared(boolean c);
}
