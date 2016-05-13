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
package us.mn.state.dot.tms.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.SQLException;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.WeatherSensor;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.WeatherPoller;

/**
 * A weather sensor is a device for sampling weather data, such as precipitation
 * rates, visibility and wind speed.
 *
 * @author Douglas Lau
 */
public class WeatherSensorImpl extends DeviceImpl implements WeatherSensor {

	/** Sample period for weather sensors (seconds) */
	static private final int SAMPLE_PERIOD_SEC = 60;

	/** Sample period for weather sensors (ms) */
	static private final int SAMPLE_PERIOD_MS = SAMPLE_PERIOD_SEC * 1000;

	/** Round an integer to the nearest 45 */
	static private Integer round45(Integer d) {
		if (d != null)
			return 45 * Math.round(d / 45.0f);
		else
			return null;
	}

	/** Load all the weather sensors */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, WeatherSensorImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new WeatherSensorImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a weather sensor */
	private WeatherSensorImpl(ResultSet row) throws SQLException {
		this(row.getString(1),		// name
		     row.getString(2),		// geo_loc
		     row.getString(3),		// controller
		     row.getInt(4),		// pin
		     row.getString(5)		// notes
		);
	}

	/** Create a weather sensor */
	private WeatherSensorImpl(String n, String l, String c, int p,
		String nt)
	{
		super(n, lookupController(c), p, nt);
		geo_loc = lookupGeoLoc(l);
		cache = new PeriodicSampleCache(PeriodicSampleType.PRECIP_RATE);
		pt_cache = new PeriodicSampleCache(
			PeriodicSampleType.PRECIP_TYPE);
		initTransients();
	}

	/** Create a new weather sensor with a string name */
	public WeatherSensorImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		g.notifyCreate();
		geo_loc = g;
		cache = new PeriodicSampleCache(PeriodicSampleType.PRECIP_RATE);
		pt_cache = new PeriodicSampleCache(
			PeriodicSampleType.PRECIP_TYPE);
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		geo_loc.notifyRemove();
	}

	/** Device location */
	private GeoLocImpl geo_loc;

	/** Get the device location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Air temp in C (null for missing) */
	private transient Integer air_temp;

	/** Get the air temp in C (null if missing) */
	@Override
	public Integer getAirTemp() {
		return air_temp;
	}

	/** Set the air temperature.
	 * @param at Air temperature in Celsius (null for missing) */
	public void setAirTempNotify(Integer at) {
		if (!integerEquals(at, air_temp)) {
			air_temp = at;
			notifyAttribute("airTemp");
		}
	}

	/** Wind speed in KPH (null if missing) */
	private transient Integer wind_speed;

	/** Get the wind speed in KPH (null if missing) */
	@Override
	public Integer getWindSpeed() {
		return wind_speed;
	}

	/** Set the wind speed in KPH */
	public void setWindSpeedNotify(Integer ws) {
		if (!integerEquals(ws, wind_speed)) {
			wind_speed = ws;
			notifyAttribute("windSpeed");
		}
	}

	/** Average wind direction in degrees (null for missing) */
	private transient Integer wind_dir;

	/** Get the average wind direction.
	 * @return Wind direction in degrees (null for missing) */
	@Override
	public Integer getWindDir() {
		return wind_dir;
	}

	/** Set the wind direction.
	 * @param wd Wind direction in degrees (null for missing). This
	 *	  angle is rounded to the nearest 45 degrees. */
	public void setWindDirNotify(Integer wd) {
		Integer a = round45(wd);
		if (!integerEquals(a, wind_dir)) {
			wind_dir = a;
			notifyAttribute("windDir");
		}
	}

	/** Cache for precipitation samples */
	private transient final PeriodicSampleCache cache;

	/** Cache for precipitation type samples */
	private transient final PeriodicSampleCache pt_cache;

	/** Accumulation of precipitation (micrometers) */
	private transient int accumulation = MISSING_DATA;

	/** Set the accumulation of precipitation (micrometers) */
	public void updateAccumulation(Integer a, long st) {
		int period = calculatePeriod(st);
		int value = calculatePrecipValue(a);
		if (period > 0 && value >= 0) {
			cache.add(new PeriodicSample(st, period, value));
			float period_h = 3600f / period;// periods per hour
			float umph = value * period_h;	// micrometers per hour
			float mmph = umph / 1000;	// millimeters per hour
			setPrecipRateNotify(Math.round(mmph));
		}
		if (value < 0)
			setPrecipRateNotify(null);
		if (period > 0 || value < 0)
			accumulation = a != null ? a : MISSING_DATA;
	}

	/** Reset the precipitation accumulation */
	public void resetAccumulation() {
		accumulation = 0;
	}

	/** Calculate the period since the last recorded sample.  If
	 * communication is interrupted, this will allow accumulated
	 * precipitation to be spread out over the appropriate samples. */
	private int calculatePeriod(long now) {
		if (stamp > 0 && now >= stamp) {
			int n = (int) (now / SAMPLE_PERIOD_MS);
			int s = (int) (stamp / SAMPLE_PERIOD_MS);
			return (n - s) * SAMPLE_PERIOD_SEC;
		} else
			return 0;
	}

	/** Calculate the precipitation since the last recorded sample.
	 * @param a New accumulated precipitation. */
	private int calculatePrecipValue(Integer a) {
		if (a != null && accumulation >= 0) {
			int val = a - accumulation;
			if (val >= 0)
				return val;
		}
		return MISSING_DATA;
	}

	/** Precipitation rate in mm/hr (null for missing) */
	private transient Integer precip_rate;

	/** Get precipitation rate in mm/hr (null for missing) */
	@Override
	public Integer getPrecipRate() {
		return precip_rate;
	}

	/** Set precipitation rate in mm/hr (null for missing) */
	private void setPrecipRateNotify(Integer pr) {
		if (!integerEquals(pr, precip_rate)) {
			precip_rate = pr;
			notifyAttribute("precipRate");
		}
	}

	/** Set the type of precipitation */
	public void setPrecipitationType(PrecipitationType pt, long st) {
		pt_cache.add(new PeriodicSample(st, SAMPLE_PERIOD_SEC,
			pt.ordinal()));
	}

	/** Visiblity in meters (null for missing) */
	private transient Integer visibility_m;

	/** Get visibility in meters (null for missing) */
	@Override
	public Integer getVisibility() {
		return visibility_m;
	}

	/** Set visibility in meters (null for missing) */
	public void setVisibilityNotify(Integer v) {
		if (!integerEquals(v, visibility_m)) {
			visibility_m = v;
			notifyAttribute("visibility");
		}
	}

	/** Time stamp from the last sample */
	private transient long stamp = 0;

	/** Get the time stamp from the last sample.
	 * @return Time as long */
	@Override
	public long getStamp() {
		return stamp;
	}

	/** Set the time stamp for the current sample */
	public void setStampNotify(long s) {
		if (s > 0 && s != stamp) {
			stamp = s;
			notifyAttribute("stamp");
		}
	}

	/** Get a weather sensor poller */
	public WeatherPoller getWeatherPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof WeatherPoller) ? (WeatherPoller) dp :null;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		WeatherPoller p = getWeatherPoller();
		if (p != null)
			p.sendRequest(this, dr);
	}

	/** Flush buffered sample data to disk */
	public void flush(PeriodicSampleWriter writer) throws IOException {
		writer.flush(cache, name);
		writer.flush(pt_cache, name);
	}

	/** Purge all samples before a given stamp. */
	public void purge(long before) {
		cache.purge(before);
		pt_cache.purge(before);
	}
}
