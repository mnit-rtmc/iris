/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
}
