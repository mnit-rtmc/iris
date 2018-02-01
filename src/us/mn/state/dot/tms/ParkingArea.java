/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
 * Parking Area interface.
 *
 * @author Douglas Lau
 */
public interface ParkingArea extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "parking_area";

	/** Get the location */
	GeoLoc getGeoLoc();

	/** Set verification camera preset 1 */
	void setPreset1(CameraPreset c);

	/** Get verification camera preset 1 */
	CameraPreset getPreset1();

	/** Set verification camera preset 2 */
	void setPreset2(CameraPreset c);

	/** Get verification camera preset 2 */
	CameraPreset getPreset2();

	/** Set verification camera preset 3 */
	void setPreset3(CameraPreset c);

	/** Get verification camera preset 3 */
	CameraPreset getPreset3();

	/** Set the site ID */
	void setSiteId(String sid);

	/** Get the site ID */
	String getSiteId();

	/** Set the relevant highway */
	void setRelevantHighway(String h);

	/** Get the relevant highway */
	String getRelevantHighway();

	/** Set the reference post */
	void setReferencePost(String p);

	/** Get the reference post */
	String getReferencePost();

	/** Set the exit ID */
	void setExitId(String x);

	/** Get the exit ID */
	String getExitId();

	/** Set the facility name */
	void setFacilityName(String n);

	/** Get the facility name */
	String getFacilityName();

	/** Set the street address */
	void setStreetAdr(String a);

	/** Get the street address */
	String getStreetAdr();

	/** Set the city */
	void setCity(String c);

	/** Get the city */
	String getCity();

	/** Set the state */
	void setState(String s);

	/** Get the state */
	String getState();

	/** Set the zip code */
	void setZip(String z);

	/** Get the zip code */
	String getZip();

	/** Set the time zone */
	void setTimeZone(String tz);

	/** Get the time zone */
	String getTimeZone();

	/** Set the ownership */
	void setOwnership(String o);

	/** Get the ownership */
	String getOwnership();

	/** Set the capacity */
	void setCapacity(Integer c);

	/** Get the capacity */
	Integer getCapacity();

	/** Set the low threshold */
	void setLowThreshold(Integer t);

	/** Get the low threshold */
	Integer getLowThreshold();

	/** Set the amenities */
	void setAmenities(String a);

	/** Get the amenities */
	String getAmenities();

	/** Get the reported available parking spaces */
	String getReportedAvailable();

	/** Get the true available parking spaces */
	Integer getTrueAvailable();

	/** Get the trend (CLEARING, STEADY, FILLING) */
	String getTrend();

	/** Set the open status */
	void setOpen(Boolean o);

	/** Get the open status */
	Boolean getOpen();

	/** Get the trust data value */
	Boolean getTrustData();

	/** Set the verified available parking spaces */
	void setVerifiedAvailable(int a);
}
