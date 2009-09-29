/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
 */
public interface CommLink extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "comm_link";

	/** Set text description */
	void setDescription(String d);

	/** Get text description */
	String getDescription();

	/** Set the remote URL */
	void setUrl(String u);

	/** Get the remote URL */
	String getUrl();

	/** Set the communication protocol */
	void setProtocol(short p);

	/** Get the communication protocol */
	short getProtocol();

	/** Set the polling timeout (milliseconds) */
	void setTimeout(int t);

	/** Get the polling timeout (milliseconds) */
	int getTimeout();

	/** Get the communication port status */
	String getStatus();

	/** Get the current line load */
	float getLoad();
}
