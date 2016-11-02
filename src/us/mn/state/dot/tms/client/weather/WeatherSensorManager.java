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
package us.mn.state.dot.tms.client.weather;

import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.DeviceManager;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * A weather sensor manager is a container for SONAR weather sensor objects.
 *
 * @author Douglas Lau
 */
public class WeatherSensorManager extends DeviceManager<WeatherSensor> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<WeatherSensor> descriptor(final Session s)
	{
		return new ProxyDescriptor<WeatherSensor>(
			s.getSonarState().getWeatherSensors(), true
		) {
			@Override
			public WeatherSensorProperties createPropertiesForm(
				WeatherSensor ws)
			{
				return new WeatherSensorProperties(s, ws);
			}
		};
	}

	/** Create a new weather sensor manager */
	public WeatherSensorManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 0);
	}

	/** Create a theme for weather sensors */
	@Override
	protected ProxyTheme<WeatherSensor> createTheme() {
		ProxyTheme<WeatherSensor> theme = new ProxyTheme<WeatherSensor>(
			this, new WeatherSensorMarker());
		theme.addStyle(ItemStyle.NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(ItemStyle.ALL);
		return theme;
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(WeatherSensor proxy) {
		return proxy.getGeoLoc();
	}
}
