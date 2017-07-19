/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
 * Copyright (C) 2011  AHMCT, University of California
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
package us.mn.state.dot.tms;

/**
 * A weather sensor is a device for sampling weather data, such as precipitation
 * rates, visibility and wind speed.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public interface WeatherSensor extends Device {

	/** SONAR type name */
	String SONAR_TYPE = "weather_sensor";

	/** Get the device location */
	GeoLoc getGeoLoc();

	/** Get air temp in C (null for missing) */
	Integer getAirTemp();

	/** Get dew point temp in C (null for missing) */
	Integer getDewPointTemp();

	/** Get max temp in C (null for missing) */
	Integer getMaxTemp();

	/** Get min temp in C (null for missing) */
	Integer getMinTemp();

	/** Get wind speed in KPH (null for missing) */
	Integer getWindSpeed();

	/** Get average wind direction in degrees (null for missing).
	 * Wind direction is the direction the wind is blowing measured 
	 * clockwise from true North, as defined by NTCIP 1204. */
	Integer getWindDir();

	/** Get wind gust speed in KPH (null for missing) */
	Integer getMaxWindGustSpeed();

	/** Get wind gust direction in degrees (null for missing).
	 * Wind direction is the direction the wind is blowing measured 
	 * clockwise from true North, as defined by NTCIP 1204. */
	Integer getMaxWindGustDir();

	/** Get spot wind direction in degrees (null for missing).
	 * Wind direction is the direction the wind is blowing measured 
	 * clockwise from true North, as defined by NTCIP 1204. */
	Integer getSpotWindDir();

	/** Get spot wind speed in KPH (null for missing) */
	Integer getSpotWindSpeed();

	/** Get precipitation rate in mm/hr (null for missing) */
	Integer getPrecipRate();

	/** Get precip situation code essPrecipSituation (null for missing) */
	Integer getPrecipSituation();

	/** Get precipitation accumulation for 1h in mm (null for missing) */
	Integer getPrecipOneHour();

	/** Get visibility in meters (null for missing) */
	Integer getVisibility();

	/** Get relative humidity as a percent (null for missing) */
	Integer getHumidity();

	/** Get the atmospheric pressure in pascals (null for missing) */
	Integer getPressure();

	/** Get the pavement surface temperature (null for missing) */
	Integer getPvmtSurfTemp();

	/** Get the surface temperature (null for missing) */
	Integer getSurfTemp();

	/** Get the pavement surface status (null for missing) */
	Integer getPvmtSurfStatus();

	/** Get the pavement surface freeze temperature (null for missing) */
	Integer getSurfFreezeTemp();

	/** Get the subsurface temperature (null for missing) */
	Integer getSubSurfTemp();

	/** Get the latest sample time stamp */
	Long getStamp();
}
