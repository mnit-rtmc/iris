/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
 * Copyright (C) 2017  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1204;

import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.METERS;
import us.mn.state.dot.tms.units.Speed;
import static us.mn.state.dot.tms.units.Speed.Units.KPH;
import static us.mn.state.dot.tms.units.Speed.Units.MPS;

/**
 * Wind sensor sample values.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class WindSensorValues {

	/** A height of 1001 is an error condition or missing value */
	static private final int HEIGHT_ERROR_MISSING = 1001;

	/** Convert height to Distance.
	 * @param h Height in meters with 1001 indicating an error or missing
	 *          value.
	 * @return Height distance or null for missing */
	static private Distance convertHeight(ASN1Integer h) {
		if (h != null) {
			int ih = h.getInteger();
			if (ih < HEIGHT_ERROR_MISSING)
				return new Distance(ih, METERS);
		}
		return null;
	}

	/** Wind dir of 361 indicates error or missing value */
	static private final int WIND_DIR_ERROR_MISSING = 361;

	/** Convert wind direction to iris format.
	 * @param wd Wind direction in degrees clockwise from north, with 361
	 *           indicating an error or missing value.
	 * @return Angle in degrees or null for missing. */
	static private Integer convertWindDir(ASN1Integer wd) {
		if (wd != null) {
			int iwd = wd.getInteger();
			if (iwd >= 0 && iwd <= 360)
				return new Integer(iwd);
		}
		return null;
	}

	/** Wind speed of 65535 indicates error or missing value */
	static private final int WIND_SPEED_ERROR_MISSING = 65535;

	/** Convert wind speed in tenths of m/s to Speed.
	 * @param ws Wind speed in tenths of meters per second, with 65535
	 *           indicating an error or missing value.
	 * @return Speed value or null if missing */
	static private Speed convertSpeed(ASN1Integer ws) {
		if (ws != null) {
			int tmps = ws.getInteger();
			if (tmps != WIND_SPEED_ERROR_MISSING) {
				double mps = 0.1 * (double) tmps;
				return new Speed(mps, MPS);
			}
		}
		return null;
	}

	/** Wind sensor height in meters */
	public final ASN1Integer wind_sensor_height = essWindSensorHeight
		.makeInt();

	/** Two minute average wind direction */
	public final ASN1Integer avg_wind_dir = essAvgWindDirection.makeInt();

	/** Two minute average wind speed */
	public final ASN1Integer avg_wind_speed = essAvgWindSpeed.makeInt();

	/** Spot wind direction */
	public final ASN1Integer spot_wind_dir = essSpotWindDirection.makeInt();

	/** Spot wind speed */
	public final ASN1Integer spot_wind_speed = essSpotWindSpeed.makeInt();

	/** Ten minute max gust wind direction */
	public final ASN1Integer gust_wind_dir = essMaxWindGustDir.makeInt();

	/** Ten minute max gust wind speed */
	public final ASN1Integer gust_wind_speed = essMaxWindGustSpeed.makeInt();

	/** Create wind sensor values */
	public WindSensorValues() {
		// FIXME: add support for sensor table (v2?)
		wind_sensor_height.setInteger(HEIGHT_ERROR_MISSING);
		avg_wind_dir.setInteger(WIND_DIR_ERROR_MISSING);
		avg_wind_speed.setInteger(WIND_SPEED_ERROR_MISSING);
		spot_wind_dir.setInteger(WIND_DIR_ERROR_MISSING);
		spot_wind_speed.setInteger(WIND_SPEED_ERROR_MISSING);
		gust_wind_dir.setInteger(WIND_DIR_ERROR_MISSING);
		gust_wind_speed.setInteger(WIND_SPEED_ERROR_MISSING);
	}

	/** Get sensor height in meters */
	public Integer getSensorHeight() {
		Distance h = convertHeight(wind_sensor_height);
		return (h != null) ? h.round(METERS) : null;
	}

	/** Get two minute average wind direction */
	public Integer getAvgWindDir() {
		return convertWindDir(avg_wind_dir);
	}

	/** Get two minute average wind speed in KPH */
	public Integer getAvgWindSpeedKPH() {
		Speed s = convertSpeed(avg_wind_speed);
		return (s != null) ? s.round(KPH) : null;
	}

	/** Get two minute average wind speed in MPS */
	private Float getAvgWindSpeedMPS() {
		Speed s = convertSpeed(avg_wind_speed);
		return (s != null) ? s.asFloat(MPS) : null;
	}

	/** Get spot wind direction */
	public Integer getSpotWindDir() {
		return convertWindDir(spot_wind_dir);
	}

	/** Get spot wind speed in KPH */
	public Integer getSpotWindSpeedKPH() {
		Speed s = convertSpeed(spot_wind_speed);
		return (s != null) ? s.round(KPH) : null;
	}

	/** Get spot wind speed in MPS */
	private Float getSpotWindSpeedMPS() {
		Speed s = convertSpeed(spot_wind_speed);
		return (s != null) ? s.asFloat(MPS) : null;
	}

	/** Get ten minute max gust wind direction */
	public Integer getGustWindDir() {
		return convertWindDir(gust_wind_dir);
	}

	/** Get ten minute max gust wind speed in KPH */
	public Integer getGustWindSpeedKPH() {
		Speed s = convertSpeed(gust_wind_speed);
		return (s != null) ? s.round(KPH) : null;
	}

	/** Get ten minute max gust wind speed in MPS */
	private Float getGustWindSpeedMPS() {
		Speed s = convertSpeed(gust_wind_speed);
		return (s != null) ? s.asFloat(MPS) : null;
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append(Json.num("wind_sensor_height", getSensorHeight()));
		sb.append(Json.num("avg_wind_dir", getAvgWindDir()));
		sb.append(Json.num("avg_wind_speed", getAvgWindSpeedMPS()));
		sb.append(Json.num("spot_wind_dir", getSpotWindDir()));
		sb.append(Json.num("spot_wind_speed", getSpotWindSpeedMPS()));
		sb.append(Json.num("gust_wind_dir", getGustWindDir()));
		sb.append(Json.num("gust_wind_speed", getGustWindSpeedMPS()));
		return sb.toString();
	}
}
