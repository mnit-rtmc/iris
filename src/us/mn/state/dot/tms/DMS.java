/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2023  Minnesota Department of Transportation
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

	/** Set the hashtags */
	void setHashtags(String[] ht);

	/** Get the hashtags */
	String[] getHashtags();

	/** Set remote beacon */
	void setBeacon(Beacon b);

	/** Get remote beacon */
	Beacon getBeacon();

	/** Set verification camera preset */
	void setPreset(CameraPreset cp);

	/** Get verification camera preset */
	CameraPreset getPreset();

	/** Set RWIS WeatherSensor override 
	 * @throws TMSException */
	void setWeatherSensorOverride(String ess);

	/** Get RWIS WeatherSensor override */
	String getWeatherSensorOverride();

	/** Get the sign configuration */
	SignConfig getSignConfig();

	/** Get the sign detail */
	SignDetail getSignDetail();

	/** Set the user sign message */
	void setMsgUser(SignMessage sm);

	/** Get the user sign message */
	SignMessage getMsgUser();

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

	/** Fault conditions.
	 *
	 * Semicolon-delimited list of fault conditions of the sign:
	 * `other`, `communications`, `power`, `attached_device`, `lamp`,
	 * `pixel`, `photocell`, `message`, `controller`, `temperature`,
	 * `climate_control`, `critical_temperature`, `drum_rotor`,
	 * `door_open`, `humidity` */
	String FAULTS = "faults";

	/** Photocell array.
	 *
	 * An array of photocell objects, one for each sensor, plus one for the
	 * composite of all sensors.  Each object has 3 fields: `description`,
	 * `error`, and `reading` */
	String PHOTOCELLS = "photocells";

	/** Light output; Integer (percentage) */
	String LIGHT_OUTPUT = "light_output";

	/** Power supplies array.
	 *
	 * An array of power supply objects, consisting of `description`,
	 * `supply_type`, `error`, `detail` and `voltage` */
	String POWER_SUPPLIES = "power_supplies";

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

	/** Get the stuck pixels as JSON */
	String getStuckPixels();

	/** Stuck-off attribute; Base64-encoded bitmap */
	String STUCK_OFF_BITMAP = "off";

	/** Stuck-on attribute; Base64-encoded bitmap */
	String STUCK_ON_BITMAP = "on";
}
