/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
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

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** Get the device location */
	GeoLoc getGeoLoc();

	/** Set static graphic (hybrid sign) */
	void setStaticGraphic(Graphic sg);

	/** Get static graphic (hybrid sign) */
	Graphic getStaticGraphic();

	/** Set remote beacon */
	void setBeacon(Beacon b);

	/** Get remote beacon */
	Beacon getBeacon();

	/** Set verification camera preset */
	void setPreset(CameraPreset cp);

	/** Get verification camera preset */
	CameraPreset getPreset();

	/** Set the RWIS sensors configured for the sign */
	void setWeatherSensors(WeatherSensor[] ess);

	/** Get the RWIS sensors configured for the sign */
	WeatherSensor[] getWeatherSensors();

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

	/** Set the lock (JSON) */
	void setLock(String lk);

	/** Get the lock (JSON) */
	String getLock();

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

	/** Cabinet temperatures array; Integer (Celsius) */
	String CABINET_TEMPS = "cabinet_temps";

	/** Ambient temperatures array; Integer (Celsius) */
	String AMBIENT_TEMPS = "ambient_temps";

	/** Housing temperatures array; Integer (Celsius) */
	String HOUSING_TEMPS = "housing_temps";

	/** Pot base; Integer (LEDSTAR only) */
	String LDC_POT_BASE = "ldc_pot_base";

	/** Pixel low current threshold; Integer (LEDSTAR only) */
	String PIXEL_CURRENT_LOW = "pixel_current_low";

	/** Pixel high current threshold; Integer (LEDSTAR only) */
	String PIXEL_CURRENT_HIGH = "pixel_current_high";

	/** Get the pixel failures (RleTable-encoded) */
	String getPixelFailures();
}
