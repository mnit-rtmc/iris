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
package us.mn.state.dot.tms.client.weather;

import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * Table model for weather sensors.
 *
 * @author Douglas Lau
 */
public class WeatherSensorModel extends ProxyTableModel<WeatherSensor> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<WeatherSensor>("Weather Sensor", 120) {
			public Object getValueAt(WeatherSensor ws) {
				return ws.getName();
			}
			public boolean isEditable(WeatherSensor ws) {
				return (ws == null) && canAdd();
			}
			public void setValueAt(WeatherSensor ws, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<WeatherSensor>("Location", 300) {
			public Object getValueAt(WeatherSensor ws) {
				return GeoLocHelper.getDescription(
					ws.getGeoLoc());
			}
		}
	    };
	}

	/** Create a new weather sensor table model */
	public WeatherSensorModel(Session s) {
		super(s, s.getSonarState().getWeatherSensors());
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return WeatherSensor.SONAR_TYPE;
	}

	/** Determine if a properties form is available */
	public boolean hasProperties() {
		return true;
	}

	/** Create a properties form for one proxy */
	protected SonarObjectForm<WeatherSensor> createPropertiesForm(
		WeatherSensor proxy)
	{
		return new WeatherSensorProperties(session, proxy);
	}
}
