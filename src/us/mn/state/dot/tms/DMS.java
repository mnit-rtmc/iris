/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
 * DMS -- Dynamic Message Sign
 *
 * @author Douglas Lau
 */
public interface DMS extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "dms";

	/** Get the device location */
	GeoLoc getGeoLoc();

	/** Set external beacon */
	void setBeacon(Beacon b);

	/** Get external beacon */
	Beacon getBeacon();

	/** Set verification camera preset */
	void setPreset(CameraPreset cp);

	/** Get verification camera preset */
	CameraPreset getPreset();

	/** Allow (or deny) sign control by Automated Warning System */
	void setAwsAllowed(boolean a);

	/** Is sign allowed to be controlled by Automated Warning System? */
	boolean getAwsAllowed();

	/** Set sign to Automated Warning System controlled */
	void setAwsControlled(boolean a);

	/** Is sign controlled by Automated Warning System? */
	boolean getAwsControlled();

	/** Get the sign configuration */
	SignConfig getSignConfig();

	/* Transient attributes (not stored in database) */

	/** Get the make */
	String getMake();

	/** Get the model */
	String getModel();

	/** Get the version */
	String getVersion();

	/** Get the minimum cabinet temperature (Celsius) */
	Integer getMinCabinetTemp();

	/** Get the maximum cabinet temperature (Celsius) */
	Integer getMaxCabinetTemp();

	/** Get the minimum ambient temperature (Celsius) */
	Integer getMinAmbientTemp();

	/** Get the maximum ambient temperature (Celsius) */
	Integer getMaxAmbientTemp();

	/** Get the minimum housing temperature (Celsius) */
	Integer getMinHousingTemp();

	/** Get the maximum housing temperature (Celsius) */
	Integer getMaxHousingTemp();

	/** Get the light output (percentage) */
	Integer getLightOutput();

	/** Index of stuck-off bitmap in pixel and lamp status arrays */
	int STUCK_OFF_BITMAP = 0;

	/** Index of stuck-on bitmap in pixel and lamp status arrays */
	int STUCK_ON_BITMAP = 1;

	/** Get the pixel status.
	 * @return Pixel status as an array of two Base64-encoded bitmaps.  The
	 *         first bitmap is "stuck off", and the second is "stuck on".
	 *         If the pixel status is not known, null is returned. */
	String[] getPixelStatus();

	/** Get power supply status.
	 * @return Power supply status as an array of strings, one for each
	 *         supply.  Each string in the array has 4 fields, seperated by
	 *         commas.  The fields are: description, supply type, status,
	 *         and detail. */
	String[] getPowerStatus();

	/** Get photocell status.
	 * @return Photocell status as an array of strings, one for each light
	 *         sensor (plus one for the composite of all sensors).  Each
	 *         string in the array has 3 fields, seperated by commas.  The
	 *         fields are: description, status, and current reading. */
	String[] getPhotocellStatus();

	/** Set the user sign message */
	void setMsgUser(SignMessage sm);

	/** Get the scheduled sign message */
	SignMessage getMsgSched();

	/** Get the current sign message */
	SignMessage getMsgCurrent();

	/** Get the message deploy time.
	 * @return Time message was deployed (ms since epoch).
	 * @see java.lang.System.currentTimeMillis */
	long getDeployTime();

	/* Manufacturer-specific attributes */

	/* LEDSTAR attributes */

	/** Set the LDC pot base (LEDSTAR) */
	void setLdcPotBase(Integer base);

	/** Get the LDC pot base (LEDSTAR) */
	Integer getLdcPotBase();

	/** Set the pixel low current threshold (LEDSTAR) */
	void setPixelCurrentLow(Integer low);

	/** Get the pixel low current threshold (LEDSTAR) */
	Integer getPixelCurrentLow();

	/** Set the pixel high curent threshold (LEDSTAR) */
	void setPixelCurrentHigh(Integer high);

	/** Get the pixel high current threshold (LEDSTAR) */
	Integer getPixelCurrentHigh();

	/* Skyline attributes */

	/** Get sign face heat tape status (Skyline) */
	String getHeatTapeStatus();
}
