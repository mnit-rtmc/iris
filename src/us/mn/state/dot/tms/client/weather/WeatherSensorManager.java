/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2018  Minnesota Department of Transportation
 * Copyright (C) 2017       Iteris Inc.
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
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.units.Angle;

/**
 * A weather sensor manager is a container for SONAR weather sensor objects.
 *
 * @author Douglas Lau
 * @author Michael Darter
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
			@Override
			public WeatherSensorForm makeTableForm() {
				return new WeatherSensorForm(s);
			}
		};
	}

	/** Create a new weather sensor manager */
	public WeatherSensorManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 12, ItemStyle.ALL);
	}

	/** Create a theme for weather sensors */
	@Override
	protected ProxyTheme<WeatherSensor> createTheme() {
		return new WeatherSensorTheme(this);
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(WeatherSensor proxy) {
		return proxy.getGeoLoc();
	}

	/** Create a map tab */
	@Override
	public WeatherSensorTab createTab() {
		return new WeatherSensorTab(session, this);
	}

	/** Get the tangent angle for the given location */
	@Override
	public Double getTangentAngle(MapGeoLoc loc) {
		WeatherSensor ws = findProxy(loc);
		if (ws != null) {
			Integer wd = ws.getWindDir();
			if (wd != null) {
				// Convert from NTCIP wind direction to 
				// Java angle transform + 90 degs for 
				// marker alignment
				return Angle.create(180 - wd).toRads();
			}
		}
		return null;
	}

	/** Check if an attribute change is interesting */
	@Override
	protected boolean checkAttributeChange(String attr) {
		return true;
	}

	/** Called when a proxy has been changed */
	@Override
	protected void proxyChangedSwing(WeatherSensor proxy, String attr) {
		super.proxyChangedSwing(proxy, attr);
		updateMarker(proxy);
	}

	/** Update marker */
	private void updateMarker(WeatherSensor ws) {
		MapGeoLoc mgl = findGeoLoc(ws);
		if (mgl != null)
			mgl.doUpdate();
	}
}
