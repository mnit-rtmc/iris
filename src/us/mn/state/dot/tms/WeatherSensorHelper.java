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

import us.mn.state.dot.sonar.Checker;

/**
 * Helper class for weather sensors.
 *
 * @author Douglas Lau
 */
public class WeatherSensorHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private WeatherSensorHelper() {
		assert false;
	}

	/** Find weather sensors using a Checker */
	static public WeatherSensor find(Checker<WeatherSensor> checker) {
		return (WeatherSensor)namespace.findObject(
			WeatherSensor.SONAR_TYPE, checker);
	}

	/** Lookup the weather sensor with the specified name */
	static public WeatherSensor lookup(String name) {
		return (WeatherSensor)namespace.lookupObject(
			WeatherSensor.SONAR_TYPE, name);
	}

	/** Test if the sensor has triggered an AWS state (e.g. high wind) */
	static public boolean isAwsState(WeatherSensor proxy) {
		return proxy.getHighWind() || proxy.getLowVisibility();
	}

	/** Get the high wind limit in kph */
	static public int getHighWindLimitKph() {
		return SystemAttrEnum.RWIS_HIGH_WIND_SPEED_KPH.getInt();
 	}

	/** Get the low visibility limit in meters */
	static public int getLowVisLimitMeters() {
		return SystemAttrEnum.RWIS_LOW_VISIBILITY_DISTANCE_M.getInt();
	}

	/** Is sensor in crazy data state? For example, wind speed is 
	 * unreasonably high. */
	static public boolean isCrazyState(WeatherSensor p) {
		if(getMaxValidWindSpeedKph() <= 0)
			return false;
		else {
			Integer ws = p.getWindSpeed();
			if(ws == null)
				return false;
			return ws > getMaxValidWindSpeedKph();
		}
	}

	/** Get the maximum valid wind speed (kph).
	 * @return Max valid wind speed (kph) or 0 for no maximum. */
	static public int getMaxValidWindSpeedKph() {
		return SystemAttrEnum.RWIS_MAX_VALID_WIND_SPEED_KPH.getInt();
	}

	/** Get the sensor observation age limit (secs).
	 * @return The sensor observation age limit. Valid observations have
	 *	   an age less than or equal to this value. Zero indicates 
	 *	   observations never expire. */
	static public int getObsAgeLimitSecs() {
		return SystemAttrEnum.RWIS_OBS_AGE_LIMIT_SECS.getInt();
 	}
}
