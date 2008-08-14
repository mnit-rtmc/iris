/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
 * Controller
 *
 * @author Douglas Lau
 */
public interface Controller extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "controller";

	/** All I/O pins */
	int ALL_PINS = 104;

	/** I/O pin for first traffic device */
	int DEVICE_PIN = 1;

	/** Set the controller cabinet */
	void setCabinet(Cabinet c);

	/** Get the controller cabinet */
	Cabinet getCabinet();

	/** Set the communication link */
	void setCommLink(CommLink l);

	/** Get the communication link */
	CommLink getCommLink();

	/** Set the drop address */
	void setDrop(short d);

	/** Get the drop address */
	short getDrop();

	/** Set the active status */
	void setActive(boolean a);

	/** Get the active status */
	boolean getActive();

	/** Set the administrator notes */
	void setNotes(String n);

	/** Get the administrator notes */
	String getNotes();

	/** Get the controller communication status */
	String getStatus();

	/** Get the controller error status */
	String getError();

	/** Get the controller firmware version */
	String getVersion();

	/** Perform a controller download */
	void setDownload(boolean reset);

	/** Test the communications to this controller */
	void setTest(boolean on_off);

	/** Get the testing status flag */
	boolean getTest();

	/** Get object IDs for IO pins */
	Integer[] getCio();
}
