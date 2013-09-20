/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.User;

/**
 * Gate Arm device interface.
 *
 * @author Douglas Lau
 */
public interface GateArm extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "gate_arm";

	/** Get the gate arm array */
	GateArmArray getGaArray();

	/** Get the index in array (1 to MAX_ARMS) */
	int getIdx();

	/** Get the version */
	String getVersion();

	/** Get the arm state */
	int getArmState();
}
