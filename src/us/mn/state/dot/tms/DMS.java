/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2022  Minnesota Department of Transportation
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

	/** Set associated GPS */
	void setGps(Gps g);

	/** Get associated GPS */
	Gps getGps();

	/** Set static graphic (hybrid sign) */
	void setStaticGraphic(Graphic sg);

	/** Get static graphic (hybrid sign) */
	Graphic getStaticGraphic();

	/** Set device purpose (ordinal of DevicePurpose) */
	void setPurpose(int p);

	/** Get device purpose (ordinal of DevicePurpose) */
	int getPurpose();

	/** Set the hidden flag */
	void setHidden(boolean h);

	/** Get the hidden flag */
	boolean getHidden();

	/** Set remote beacon */
	void setBeacon(Beacon b);

	/** Get remote beacon */
	Beacon getBeacon();

	/** Set verification camera preset */
	void setPreset(CameraPreset cp);

	/** Get verification camera preset */
	CameraPreset getPreset();

	/** Get the sign configuration */
	SignConfig getSignConfig();

	/** Get the sign detail */
	SignDetail getSignDetail();

	/** Set the override font */
	void setOverrideFont(Font f);

	/** Get the override font */
	Font getOverrideFont();

	/** Set override foreground color (24-bit rgb) */
	void setOverrideForeground(Integer fg);

	/** Get override foreground color (24-bit rgb) */
	Integer getOverrideForeground();

	/** Set override background color (24-bit rgb) */
	void setOverrideBackground(Integer bg);

	/** Get override background color (24-bit rgb) */
	Integer getOverrideBackground();

	/** Set the user sign message */
	void setMsgUser(SignMessage sm);

	/** Get the scheduled sign message */
	SignMessage getMsgSched();

	/** Get the current sign message */
	SignMessage getMsgCurrent();

	/** Get current message expiration time.
	 * @return Expiration time for the current message (ms since epoch), or
	 *         null for no expiration.
	 * @see java.lang.System#currentTimeMillis */
	Long getExpireTime();

	/** Get the current status as JSON */
	String getStatus();

	/** Status JSON attributes */

	/** Light output; Integer (percentage) */
	String LIGHT_OUTPUT = "light_output";

	/** Minimum cabinet temperature; Integer (Celsius) */
	String CABINET_TEMP_MIN = "cabinet_temp_min";

	/** Maximum cabinet temperature; Integer (Celsius) */
	String CABINET_TEMP_MAX = "cabinet_temp_max";

	/** Minimum ambient temperature; Integer (Celsius) */
	String AMBIENT_TEMP_MIN = "ambient_temp_min";

	/** Maximum ambient temperature; Integer (Celsius) */
	String AMBIENT_TEMP_MAX = "ambient_temp_max";

	/** Minimum housing temperature; Integer (Celsius) */
	String HOUSING_TEMP_MIN = "housing_temp_min";

	/** Maximum housing temperature; Integer (Celsius) */
	String HOUSING_TEMP_MAX = "housing_temp_max";

	/** Pot base; Integer (LEDSTAR only) */
	String LDC_POT_BASE = "lcd_pot_base";

	/** Pixel low current threshold; Integer (LEDSTAR only) */
	String PIXEL_CURRENT_LOW = "pixel_current_low";

	/** Pixel high current threshold; Integer (LEDSTAR only) */
	String PIXEL_CURRENT_HIGH = "pixel_current_high";

	/* Transient attributes (not stored in database) */

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
}
