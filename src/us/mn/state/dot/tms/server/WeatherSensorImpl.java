/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2011  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Constants;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.WeatherPoller;

/**
 * A weather sensor is a device for sampling weather data, such as precipitation
 * rates, visibility and wind speed.
 *
 * @author Douglas Lau
 */
public class WeatherSensorImpl extends DeviceImpl implements WeatherSensor {

	/** Sample period for weather sensors (seconds) */
	static protected final int SAMPLE_PERIOD_SEC = 60;

	/** Sample period for weather sensors (ms) */
	static protected final int SAMPLE_PERIOD_MS = SAMPLE_PERIOD_SEC * 1000;

	/** Create a cache for periodic sample data */
	static protected PeriodicSampleCache createCache(String n) {
		return new PeriodicSampleCache.SixteenBit(
			new SampleArchiveFactoryImpl(n, ".pr60"),
			SAMPLE_PERIOD_SEC);
	}

	/** Create a cache for precipitation type sample data */
	static protected PeriodicSampleCache createPtCache(String n) {
		return new PeriodicSampleCache.EightBit(
			new SampleArchiveFactoryImpl(n, ".pt60"),
			SAMPLE_PERIOD_SEC);
	}

	/** Load all the weather sensors */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading weather sensors...");
		namespace.registerType(SONAR_TYPE, WeatherSensorImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new WeatherSensorImpl(
					namespace,
					row.getString(1),	// name
					row.getString(2),	// geo_loc
					row.getString(3),	// controller
					row.getInt(4),		// pin
					row.getString(5)	// notes
				));
			}
		});
	}

	/** Get a mapping of the columns */
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
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new weather sensor with a string name */
	public WeatherSensorImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		MainServer.server.createObject(g);
		geo_loc = g;
		cache = createCache(n);
		pt_cache = createPtCache(n);
	}

	/** Create a weather sensor */
	protected WeatherSensorImpl(String n, GeoLocImpl l, ControllerImpl c,
		int p, String nt)
	{
		super(n, c, p, nt);
		geo_loc = l;
		cache = createCache(n);
		pt_cache = createPtCache(n);
		initTransients();
	}

	/** Create a weather sensor */
	protected WeatherSensorImpl(Namespace ns, String n, String l, String c,
		int p, String nt)
	{
		this(n, (GeoLocImpl)ns.lookupObject(GeoLoc.SONAR_TYPE, l),
		     (ControllerImpl)ns.lookupObject(Controller.SONAR_TYPE, c),
		     p, nt);
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		super.doDestroy();
		MainServer.server.removeObject(geo_loc);
	}

	/** Device location */
	protected GeoLocImpl geo_loc;

	/** Get the device location */
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Get a weather sensor poller */
	public WeatherPoller getWeatherPoller() {
		if(isActive()) {
			MessagePoller p = getPoller();
			if(p instanceof WeatherPoller)
				return (WeatherPoller)p;
		}
		return null;
	}

	/** Request a device operation */
	public void setDeviceRequest(int r) {
		// no device requests are currently supported
	}

	/** Cache for precipitation samples */
	protected transient final PeriodicSampleCache cache;

	/** Cache for precipitation type samples */
	protected transient final PeriodicSampleCache pt_cache;

	/** Accumulation of precipitation (micrometers) */
	protected transient int accumulation = Constants.MISSING_DATA;

	/** Time stamp of last sample */
	protected transient long stamp = TimeSteward.currentTimeMillis();

	/** Set the accumulation of precipitation */
	public void setAccumulation(int a) {
		long now = TimeSteward.currentTimeMillis();
		int period = calculatePeriod(now);
		int value = calculateValue(a);
		if(period > 0 && value >= 0)
			cache.addSample(new PeriodicSample(now, period, value));
		if(period > 0 || value < 0) {
			accumulation = a;
			stamp = now;
		}
	}

	/** Calculate the period since the last recorded sample.  If
	 * communication is interrupted, this will allow accumulated
	 * precipitation to be spread out over the appropriate samples. */
	protected int calculatePeriod(long now) {
		int n = (int)(now / SAMPLE_PERIOD_MS);
		int s = (int)(stamp / SAMPLE_PERIOD_MS);
		return (n - s) * SAMPLE_PERIOD_SEC;
	}

	/** Calculate the precipitation since the last recorded sample.
	 * @param a New accumulated precipitation. */
	protected int calculateValue(int a) {
		if(accumulation >= 0) {
			int val = a - accumulation;
			if(val >= 0)
				return val;
		}
		return Constants.MISSING_DATA;
	}

	/** Set the type of precipitation */
	public void setPrecipitationType(PrecipitationType pt) {
		long now = TimeSteward.currentTimeMillis();
		pt_cache.addSample(new PeriodicSample(now, SAMPLE_PERIOD_SEC,
			pt.ordinal()));
	}

	/** Flush buffered sample data to disk */
	public void flush() {
		try {
			cache.flush();
			pt_cache.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
