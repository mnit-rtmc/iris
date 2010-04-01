/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
 * Incident detail provide more information about an incident.  This is used
 * to log information such as "debris" or "grass fire".
 *
 * @author Douglas Lau
 */
public interface IncidentDetail extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "incident_detail";

	/** Set the description */
	void setDescription(String d);

	/** Get the description */
	String getDescription();
}
