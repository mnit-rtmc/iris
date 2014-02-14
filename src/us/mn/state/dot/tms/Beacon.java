/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2014  Minnesota Department of Transportation
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

	/** Set the verification camera */
	void setCamera(Camera c);

	/** Get the verification camera */
	Camera getCamera();

	/** Set the message text */
	void setMessage(String t);

	/** Get the message text */
	String getMessage();

	/** Set the flashing state of the beacon */
	void setFlashing(boolean f);

	/** Check if the beacon is flashing */
	boolean getFlashing();
}
