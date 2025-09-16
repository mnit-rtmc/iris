/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2025  Minnesota Department of Transportation
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
 * Gate Arm device interface.
 *
 * @author Douglas Lau
 */
public interface GateArm extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "gate_arm";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** Get the device location */
	GeoLoc getGeoLoc();

	/** Set verification camera preset */
	void setPreset(CameraPreset cp);

	/** Get verification camera preset */
	CameraPreset getPreset();

	/** Set the opposing traffic flag */
	void setOpposing(boolean ot);

	/** Get the opposing traffic flag */
	boolean getOpposing();

	/** Set downstream hashtag */
	void setDownstream(String ds);

	/** Get downstream hashtag */
	String getDownstream();

	/** Get the arm state */
	int getArmState();

	/** Get the interlock ordinal */
	int getInterlock();

	/** Get fault description (or null) */
	String getFault();

	/** Set the lock (JSON) */
	void setLock(String lk);
}
