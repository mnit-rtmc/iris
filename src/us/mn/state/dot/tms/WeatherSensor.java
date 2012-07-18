/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
 * Copyright (C) 2011  AHMCT, University of California
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

	/** Get the latest time stamp or MISSING_DATA */
	long getStamp();

	/** Get the visibility in meters or null for missing */
	Integer getVisibility();

	/** Get the wind speed in KPH or null for missing */
	Integer getWindSpeed();

	/** Get the air temp in C or null for missing */
	Integer getAirTemp();

	/** Get the average wind direction in degrees or null for missing */
	Integer getWindDirAvg();

	/** Get the precipitation rate in mm/hr or null for missing */
	Integer getPrecipRate();

	/** Get the expired state */
	boolean getExpired();

	/** Get the high wind state */
	boolean getHighWind();

	/** Get the low visibility state */
	boolean getLowVisibility();
}
