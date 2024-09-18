/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
 * Copyright (C) 2018-2024  Minnesota Department of Transportation
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
 * Interface for a GPS (Global Positioning System) device
 *
 * @author John L. Stanley
 * @author Douglas Lau
 */
public interface Gps extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "gps";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = Controller.SONAR_TYPE;

	/** Set the associated device location */
	void setGeoLoc(GeoLoc l);

	/** Get the associated device location */
	GeoLoc getGeoLoc();

	/** Get the latest poll date &amp; time */
	Long getLatestPoll();

	/** Get the latest sample date &amp; time */
	Long getLatestSample();

	/** Get the most recent latitude */
	Double getLat();

	/** Get the most recent longitude */
	Double getLon();
}
