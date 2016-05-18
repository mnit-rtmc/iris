/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.WeatherSensorImpl;

/**
 * WeatherPoller is an interface for polling weather data sensors for
 * precipitation, visibility, wind speed, etc.
 *
 * @author Douglas Lau
 */
public interface WeatherPoller {

	/** Send a device request */
	void sendRequest(WeatherSensorImpl ws, DeviceRequest r);

	/** Send settings to a weather sensor */
	void sendSettings(WeatherSensorImpl ws);
}
