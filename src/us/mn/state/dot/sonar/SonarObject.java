/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2017  Minnesota Department of Transportation
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
 * This interface must be implemented by classes stored in a SONAR namespace.
 * Any implementation class which allows SONAR clients to create objects must
 * also define a public constructor with one String argument, "name", and a
 * <code>public void doStore()</code> method for storing objects in a database.
 *
 * @author Douglas Lau
 */
public interface SonarObject {

	/** Get the SONAR type name */
	String getTypeName();

	/** Get the SONAR object name */
	String getName();

	/** Destroy the SONAR object */
	void destroy();
}
