/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
 * A WarningSign is a traffic device can display one fixed message. It can
 * only be turned on or off.
 *
 * @author Douglas Lau
 */
public interface WarningSign extends Device2 {

	/** SONAR type name */
	String SONAR_TYPE = "warning_sign";

	/** Set the verification camera */
	void setCamera(Camera c);

	/** Get the verification camera */
	Camera getCamera();

	/** Set the message text */
	void setMessage(String t);

	/** Get the message text */
	String getMessage();

	/** Set the deployed status of the sign */
	void setDeployed(boolean d);

	/** Check if the warning sign is deployed */
	boolean getDeployed();
}
