/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2016  Minnesota Department of Transportation
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
 * A Beacon is a light which flashes toward oncoming traffic.
 *
 * @author Douglas Lau
 */
public interface Beacon extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "beacon";

	/** Get the device location */
	GeoLoc getGeoLoc();

	/** Set the verification camera preset */
	void setPreset(CameraPreset cp);

	/** Get the verification camera preset */
	CameraPreset getPreset();

	/** Set the message text */
	void setMessage(String t);

	/** Get the message text */
	String getMessage();

	/** Set the controller I/O verify pin number */
	void setVerifyPin(Integer p);

	/** Get the controller I/O verify pin number */
	Integer getVerifyPin();

	/** Set the flashing state of the beacon */
	void setFlashing(boolean f);

	/** Check if the beacon is flashing */
	boolean getFlashing();
}
