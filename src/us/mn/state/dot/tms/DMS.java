/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
public interface DMS extends Device2 {

	/** SONAR type name */
	String SONAR_TYPE = "dms";

	/** Get the device location */
	GeoLoc getGeoLoc();

	/** Set the travel time message template */
	void setTravel(String t);

	/** Get the travel time message template */
	String getTravel();

	/** Set array of all timing plans */
	void setTimingPlans(TimingPlan[] p);

	/** Get array of all timing plans */
	TimingPlan[] getTimingPlans();

	/** Set verification camera */
	void setCamera(Camera c);

	/** Get verification camera */
	Camera getCamera();

	/* Transient attributes (not stored in database) */

	/** Get the make */
	String getMake();

	/** Get the model */
	String getModel();

	/** Get the version */
	String getVersion();

	/** Get sign access description */
	String getSignAccess();

	/** Get DMS type */
	int getDmsType();

	/** Get sign legend */
	String getLegend();

	/** Get beacon type description */
	String getBeaconType();

	/** Get sign technology description */
	String getTechnology();

	/** Get height of the sign face (mm) */
	Integer getFaceHeight();

	/** Get width of the sign face (mm) */
	Integer getFaceWidth();

	/** Get horizontal border (mm) */
	Integer getHorizontalBorder();

	/** Get vertical border (mm) */
	Integer getVerticalBorder();

	/** Get horizontal pitch (mm) */
	Integer getHorizontalPitch();

	/** Get vertical pitch (mm) */
	Integer getVerticalPitch();

	/** Get sign height (pixels) */
	Integer getHeightPixels();

	/** Get sign width (pixels) */
	Integer getWidthPixels();

	/** Get character height (pixels) */
	Integer getCharHeightPixels();

	/** Get character width (pixels) */
	Integer getCharWidthPixels();

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

	/** Get the lamp status.
	 * @return Lamp status as an array of two Base64-encoded bitmaps.  The
	 *         first bitmap is "stuck off", and the second is "stuck on".
	 *         If the lamp status is not known, null is returned. */
	String[] getLampStatus();

	/** Request a sign operation (query message, test pixels, etc.) */
	void setSignRequest(int r);

	/** Get user note */
	String getUserNote();

	/** Set the next sign message */
	void setMessageNext(SignMessage m);

	/** Get the next (in process) sign message */
	SignMessage getMessageNext();

	/** Get the current sign message */
	SignMessage getMessageCurrent();

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

	/** Get power supply enum values (Skyline) */
	String[] getPowerStatus();
}
