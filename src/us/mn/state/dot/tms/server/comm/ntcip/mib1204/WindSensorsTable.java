/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2022  Minnesota Department of Transportation
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

/**
 * Wind sensors data table, where each table row contains data read from a
 * single wind sensor within the same controller.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class WindSensorsTable {

	/** Wind sensor height in meters (deprecated in V2) */
	public final HeightObject wind_sensor_height = new HeightObject(
		"wind_sensor_height", essWindSensorHeight.makeInt());

	/** Two minute average wind direction (deprecated in V2) */
	public final DirectionObject avg_wind_dir = new DirectionObject(
		"avg_wind_dir", essAvgWindDirection.makeInt());

	/** Two minute average wind speed (deprecated in V2) */
	public final WindSpeedObject avg_wind_speed = new WindSpeedObject(
		"avg_wind_speed", essAvgWindSpeed.makeInt());

	/** Spot wind direction (deprecated in V2) */
	public final DirectionObject spot_wind_dir = new DirectionObject(
		"spot_wind_dir", essSpotWindDirection.makeInt());

	/** Spot wind speed (deprecated in V2) */
	public final WindSpeedObject spot_wind_speed = new WindSpeedObject(
		"spot_wind_speed", essSpotWindSpeed.makeInt());

	/** Ten minute max gust wind direction (deprecated in V2) */
	public final DirectionObject gust_wind_dir = new DirectionObject(
		"gust_wind_dir", essMaxWindGustDir.makeInt());

	/** Ten minute max gust wind speed (deprecated in V2) */
	public final WindSpeedObject gust_wind_speed = new WindSpeedObject(
		"gust_wind_speed", essMaxWindGustSpeed.makeInt());

	/** Get two minute average wind direction */
	public Integer getAvgWindDir() {
		return avg_wind_dir.getDirection();
	}

	/** Get two minute average wind speed in KPH */
	public Integer getAvgWindSpeedKPH() {
		return avg_wind_speed.getSpeedKPH();
	}

	/** Get spot wind direction */
	public Integer getSpotWindDir() {
		return spot_wind_dir.getDirection();
	}

	/** Get spot wind speed in KPH */
	public Integer getSpotWindSpeedKPH() {
		return spot_wind_speed.getSpeedKPH();
	}

	/** Get ten minute max gust wind direction */
	public Integer getGustWindDir() {
		return gust_wind_dir.getDirection();
	}

	/** Get ten minute max gust wind speed in KPH */
	public Integer getGustWindSpeedKPH() {
		return gust_wind_speed.getSpeedKPH();
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append(wind_sensor_height.toJson());
		sb.append(avg_wind_dir.toJson());
		sb.append(avg_wind_speed.toJson());
		sb.append(spot_wind_dir.toJson());
		sb.append(spot_wind_speed.toJson());
		sb.append(gust_wind_dir.toJson());
		sb.append(gust_wind_speed.toJson());
		return sb.toString();
	}
}
