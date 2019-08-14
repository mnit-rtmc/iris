/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar;

/**
 * A connection encapsulates the state of one client connection on the server.
 *
 * @author Douglas Lau
 */
public interface Connection extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "connection";

	/** Get the user logged in on the connection.
	 * May be null (before a successful login). */
	User getUser();

	/** Get the SONAR session ID */
	long getSessionId();
}
