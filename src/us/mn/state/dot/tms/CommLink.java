/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2020  Minnesota Department of Transportation
 * Copyright (C) 2015  AHMCT, University of California
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
 * A CommLink is a network connection for device communication.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public interface CommLink extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "comm_link";

	/** Set text description */
	void setDescription(String d);

	/** Get text description */
	String getDescription();

	/** Set the remote URI */
	void setUri(String u);

	/** Get the remote URI */
	String getUri();

	/** Enable or disable polling */
	void setPollEnabled(boolean e);

	/** Get polling enabled/disabled flag */
	boolean getPollEnabled();

	/** Set the comm configuration */
	void setCommConfig(CommConfig cc);

	/** Get the comm configuration */
	CommConfig getCommConfig();

	/** Get the communication port status */
	String getStatus();
}
