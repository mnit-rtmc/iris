/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
 * A Lane-Use Control Signal Array is a series of devices across roadway lanes
 * which can display indications such as "lane open", "lane closed", etc.
 *
 * @author Douglas Lau
 */
public interface Lcs extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "lcs";

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

	/** Set the LCS type */
	void setLcsType(int t);

	/** Get the LCS type */
	int getLcsType();

	/** Set the lane shift of left-side LCS */
	void setShift(int sh);

	/** Get the lane shift of left-side LCS */
	int getShift();

	/** Set the lock (JSON) */
	void setLock(String lk);

	/** Get the lock (JSON) */
	String getLock();

	/** Get the current status (JSON) */
	String getStatus();

	/** Status JSON attributes: INDICATIONS, FAULT */

	/** Indications (array of LcsIndication ordinal values) */
	String INDICATIONS = "indications";

	/** Fault conditions */
	String FAULTS = "faults";
}
