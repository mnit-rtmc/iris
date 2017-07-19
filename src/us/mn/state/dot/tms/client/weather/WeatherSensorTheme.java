/*
 * IRIS -- Intelligent Roadway Information System
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
package us.mn.state.dot.tms.client.weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.Color;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.client.ToolTipBuilder;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.units.Angle;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.units.Temperature;
import us.mn.state.dot.tms.units.Pressure;
import us.mn.state.dot.tms.units.Speed;

/**
 * Theme for weather sensor objects on the map.
 *
 * @author Michael Darter
 */
public class WeatherSensorTheme extends ProxyTheme<WeatherSensor> {

	/** Create a new proxy theme */
	public WeatherSensorTheme(ProxyManager<WeatherSensor> m) {
		super(m, new WeatherSensorMarker());
		addStyle(ItemStyle.FAILED, 
			ProxyTheme.COLOR_FAILED);
		addStyle(ItemStyle.MAINTENANCE,
			ProxyTheme.COLOR_UNAVAILABLE);
		addStyle(ItemStyle.ACTIVE, 
			ProxyTheme.COLOR_AVAILABLE);
		if (SystemAttrEnum.DMS_AWS_ENABLE.getBoolean()) {
			addStyle(ItemStyle.AWS_DEPLOYED, Color.RED);
		}
		addStyle(ItemStyle.ALL, Color.WHITE);
	}

	/** Get tooltip text for the given map object.
	 * @return String or null for none */
	public String getTip(MapObject o) {
		WeatherSensor p = manager.findProxy(o);
		if(p == null)
			return null;
		ToolTipBuilder ttb = new ToolTipBuilder();
		ttb.addLine(manager.getDescription(p));
		// TODO move tip text to i18n
		ttb.addLine("Air temperature", 
			Temperature.create(p.getAirTemp(), 
			Temperature.Units.CELSIUS));
		ttb.addLine("Avg. wind speed", 
			Speed.create(p.getWindSpeed(), 
			Speed.Units.KPH));
		ttb.addLine("Max wind gust speed", 
			Speed.create(p.getMaxWindGustSpeed(), 
			Speed.Units.KPH));
		ttb.addLine("Avg. wind direction", 
			Angle.create(p.getWindDir()));
		ttb.addLine("Visibility", 
			Distance.create(p.getVisibility(), 
			Distance.Units.METERS));
		ttb.addLine("Relative humidity", 
			p.getHumidity(), "%");
		ttb.addLine("Barometric pressure",
			Pressure.create(p.getPressure()));
		ttb.addLine("Precipitation rate", 
			p.getPrecipRate(), "mm/hr");
		ttb.addLine("Precipitation 1h", 
			p.getPrecipRate(), "mm");
		ttb.addLine("Dew point temperature", 
			Temperature.create(p.getDewPointTemp()));
		ttb.addLine("Pavement surface temperature", 
			Temperature.create(p.getPvmtSurfTemp()));
		ttb.addLine("Surface temperature", 
			Temperature.create(p.getSurfTemp()));
		ttb.addLine("Subsurface temperature", 
			Temperature.create(p.getSubSurfTemp()));
		ttb.addLine("Time", 
			formatDateString(p.getStamp()));
		ttb.setLast();
		return ttb.get();
	}

	/** Return the specified date as a string in local time.
	 * @param stamp A time stamp, null or < 0 for missing
	 * @return A string in local time as HH:mm:ss MM-dd-yyyy */
	static private String formatDateString(Long stamp) {
		if (stamp == null || stamp < 0)
			return "";
		Date d = new Date(stamp);
		return new SimpleDateFormat("HH:mm:ss MM-dd-yyyy").format(d);
	}
}
