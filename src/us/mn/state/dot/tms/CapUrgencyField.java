/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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
 * Common Alerting Protocol (CAP) urgency field substitution values.  Used for
 * IPAWS alert processing for generating messages for posting to DMS.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public interface CapUrgencyField extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "cap_urgency";

	/** Default Event (for use if no other matching event found) */
	String DEFAULT_EVENT = "<default>";

	/** Set the applicable alert event type */
	void setEvent(String ev);

	/** Get the applicable alert event type */
	String getEvent();

	/** Set the urgency ordinal value */
	void setUrgency(int u);

	/** Get the urgency ordinal value */
	int getUrgency();

	/** Set the MULTI string that will be substituted into the message */
	void setMulti(String m);

	/** Get the MULTI string that will be substituted into the message */
	String getMulti();
}
