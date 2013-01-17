/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2013  Minnesota Department of Transportation
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

/**
 * Alarm is a class for reading alarm inputs on controllers.
 *
 * @author Douglas Lau
 */
public interface Alarm extends ControllerIO {

	/** SONAR type name */
	String SONAR_TYPE = "alarm";

	/** Set the description */
	void setDescription(String d);

	/** Get the description */
	String getDescription();

	/** Get the state of the alarm */
	boolean getState();

	/** Get the most recent alarm trigger time. This time is in
	 * milliseconds since the epoch. */
	Long getTriggerTime();
}
