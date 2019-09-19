/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.units.Speed;
import static us.mn.state.dot.tms.units.Speed.Units.KPH;
import static us.mn.state.dot.tms.units.Speed.Units.MPH;
import static us.mn.state.dot.tms.units.Speed.Units.MPS;

/**
 * Wind sensor sample values.
 *
 * @author Douglas Lau
 */
public class WindSensorValues {

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
	static private final int WIND_SPEED_INVALID_MISSING = 65535;

	/** Convert wind speed in tenths of m/s to Speed.
	 * @param ws Wind speed in tenths of meters per second, with 65535
	 *           indicating an error or missing value.
	 * @return Speed value or null if missing */
	static private Speed convertSpeed(ASN1Integer ws) {
		if (ws != null) {
			int tmps = ws.getInteger();
			if (tmps != WIND_SPEED_INVALID_MISSING) {
				double mps = 0.1 * (double) tmps;
				return new Speed(mps, MPS);
			}
		}
		return null;
	}

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

	/** Get two minute average wind direction */
	public Integer getAvgWindDir() {
		return convertWindDir(avg_wind_dir);
	}

	/** Get two minute average wind speed in KPH */
	public Integer getAvgWindSpeedKPH() {
		Speed s = convertSpeed(avg_wind_speed);
		return (s != null) ? s.round(KPH) : null;
	}

	/** Get two minute average wind speed in MPH */
	public Integer getAvgWindSpeedMPH() {
		Speed s = convertSpeed(avg_wind_speed);
		return (s != null) ? s.round(MPH) : null;
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

	/** Get spot wind speed in MPH */
	public Integer getSpotWindSpeedMPH() {
		Speed s = convertSpeed(spot_wind_speed);
		return (s != null) ? s.round(MPH) : null;
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

	/** Get ten minute max gust wind speed in MPH */
	public Integer getGustWindSpeedMPH() {
		Speed s = convertSpeed(gust_wind_speed);
		return (s != null) ? s.round(MPH) : null;
	}
}
