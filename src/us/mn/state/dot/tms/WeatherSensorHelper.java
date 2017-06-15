/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2015  Minnesota Department of Transportation
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

import java.util.Iterator;
import us.mn.state.dot.sched.TimeSteward;

/**
 * Helper class for weather sensors.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class WeatherSensorHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private WeatherSensorHelper() {
		assert false;
	}

	/** Lookup the weather sensor with the specified name */
	static public WeatherSensor lookup(String name) {
		return (WeatherSensor)namespace.lookupObject(
			WeatherSensor.SONAR_TYPE, name);
	}

	/** Get a weather sensor iterator */
	static public Iterator<WeatherSensor> iterator() {
		return new IteratorWrapper<WeatherSensor>(namespace.iterator(
			WeatherSensor.SONAR_TYPE));
	}

	/** Test if the sensor has triggered an AWS state (e.g. high wind) */
	static public boolean isAwsState(WeatherSensor proxy) {
		return isHighWind(proxy) || isLowVisibility(proxy);
	}

	/** Get the high wind limit in kph */
	static public int getHighWindLimitKph() {
		return SystemAttrEnum.RWIS_HIGH_WIND_SPEED_KPH.getInt();
 	}

	/** Is wind speed high? */
	static public boolean isHighWind(WeatherSensor ws) {
		if(isSampleExpired(ws))
			return false;
		Integer s = ws.getWindSpeed();
		if(s == null)
			return false;
		int t = getHighWindLimitKph();
		int m = getMaxValidWindSpeedKph();
		if(m <= 0)
			return s > t;
		else
			return s > t && s <= m;
	}

	/** Get the low visibility limit in meters */
	static public int getLowVisLimitMeters() {
		return SystemAttrEnum.RWIS_LOW_VISIBILITY_DISTANCE_M.getInt();
	}

	/** Is visibility low? */
	static public boolean isLowVisibility(WeatherSensor ws) {
		if(isSampleExpired(ws))
			return false;
		Integer v = ws.getVisibility();
		return v != null && v < getLowVisLimitMeters();
	}

	/** Get the maximum valid wind speed (kph).
	 * @return Max valid wind speed (kph) or 0 for no maximum. */
	static public int getMaxValidWindSpeedKph() {
		return SystemAttrEnum.RWIS_MAX_VALID_WIND_SPEED_KPH.getInt();
	}

	/** Check if the sample data has expired */
	static public boolean isSampleExpired(WeatherSensor ws) {
		if (ws != null) {
			Long st = ws.getStamp();
			if (st == null)
				return false;
			return st + getObsAgeLimitSecs() * 1000 <
				TimeSteward.currentTimeMillis();
		} else
			return false;
	}

	/** Get the sensor observation age limit (secs).
	 * @return The sensor observation age limit. Valid observations have
	 *	   an age less than or equal to this value. Zero indicates 
	 *	   observations never expire. */
	static public int getObsAgeLimitSecs() {
		return SystemAttrEnum.RWIS_OBS_AGE_LIMIT_SECS.getInt();
 	}

	/** Get a valid precipitation rate, or null */
	static public Integer getPrecipRate(WeatherSensor ws) {
		return (isSampleExpired(ws))
		      ? null
		      : ws.getPrecipRate();
	}
}
