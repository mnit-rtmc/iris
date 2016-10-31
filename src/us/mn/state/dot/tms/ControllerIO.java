/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
 * ControllerIO is an input/output connected to a controller (alarm or device)
 *
 * @author Douglas Lau
 */
public interface ControllerIO extends SonarObject {

	/** Set the controller for the I/O */
	void setController(Controller c);

	/** Get the controller for the I/O */
	Controller getController();

	/** Set the controller I/O pin number */
	void setPin(int p);

	/** Get the controller I/O pin number */
	int getPin();

	/** Get item style bits */
	long getStyles();
}
