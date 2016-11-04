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

import java.util.ArrayList;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for weather sensors.
 *
 * @author Douglas Lau
 */
public class WeatherSensorModel extends ProxyTableModel<WeatherSensor> {

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<WeatherSensor>> createColumns() {
		ArrayList<ProxyColumn<WeatherSensor>> cols =
			new ArrayList<ProxyColumn<WeatherSensor>>(2);
		cols.add(new ProxyColumn<WeatherSensor>("weather_sensor", 120) {
			public Object getValueAt(WeatherSensor ws) {
				return ws.getName();
			}
		});
		cols.add(new ProxyColumn<WeatherSensor>("location", 300) {
			public Object getValueAt(WeatherSensor ws) {
				return GeoLocHelper.getDescription(
					ws.getGeoLoc());
			}
		});
		return cols;
	}

	/** Create a new weather sensor table model */
	public WeatherSensorModel(Session s) {
		super(s, WeatherSensorManager.descriptor(s));
	}

	/** Get the visible row count */
	@Override
	public int getVisibleRowCount() {
		return 12;
	}
}
