/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.sql.ResultSet;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Angle;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Length;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.WeatherPoller;

/**
 * A weather sensor is a device for sampling weather data, such as precipitation
 * rates, visibility and wind speed.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class WeatherSensorImpl extends DeviceImpl implements WeatherSensor {

	/** Sample period for weather sensors (seconds) */
	static protected final int SAMPLE_PERIOD_SEC = 60;

	/** Sample period for weather sensors (ms) */
	static protected final int SAMPLE_PERIOD_MS = SAMPLE_PERIOD_SEC * 1000;

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
	public WeatherSensorImpl(String n) 
		throws TMSException, SonarException 
	{
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		MainServer.server.createObject(g);
		geo_loc = g;
		cache = new PeriodicSampleCache(
			PeriodicSampleType.PRECIP_RATE, n);
		pt_cache = new PeriodicSampleCache(
			PeriodicSampleType.PRECIP_TYPE, n);
	}

	/** Create a weather sensor */
	protected WeatherSensorImpl(String n, GeoLocImpl l, ControllerImpl c,
		int p, String nt)
	{
		super(n, c, p, nt);
		geo_loc = l;
		cache = new PeriodicSampleCache(
			PeriodicSampleType.PRECIP_RATE, n);
		pt_cache = new PeriodicSampleCache(
			PeriodicSampleType.PRECIP_TYPE, n);
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
	protected transient int accumulation = MISSING_DATA;

	/** Time stamp from the last sample */
	protected transient long last_stamp = MISSING_DATA;

	/** Get the current stamp.
	 * @return Time as long or MISSING_DATA */
	public long getStamp() {
		return last_stamp;
	}

	/** Set the current stamp or MISSING_DATA */
	public void setStamp(long s) {
		if(s != last_stamp) {
			last_stamp = s;
			notifyAttribute("stamp");
		}
	}

	/** Set the accumulation of precipitation */
	public void setAccumulation(int a) {
		long now = TimeSteward.currentTimeMillis();
		int period = calculatePeriod(now);
		int value = calculateValue(a);
		if(period > 0 && value >= 0)
			cache.add(new PeriodicSample(now, period, value));
		if(period > 0 || value < 0) {
			accumulation = a;
			last_stamp = now;
		}
	}

	/** Calculate the period since the last recorded sample.  If
	 * communication is interrupted, this will allow accumulated
	 * precipitation to be spread out over the appropriate samples. */
	protected int calculatePeriod(long now) {
		int n = (int)(now / SAMPLE_PERIOD_MS);
		int s = (int)(last_stamp / SAMPLE_PERIOD_MS);
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
		return MISSING_DATA;
	}

	/** Set the type of precipitation */
	public void setPrecipitationType(PrecipitationType pt) {
		long now = TimeSteward.currentTimeMillis();
		pt_cache.add(new PeriodicSample(now, SAMPLE_PERIOD_SEC,
			pt.ordinal()));
	}

	/** Flush buffered sample data to disk */
	public void flush(PeriodicSampleWriter writer) {
		try {
			writer.flush(cache);
			writer.flush(pt_cache);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** Visiblity in meters or null for missing */
	private transient Integer latest_visibility;

	/** Get the visibility in meters or null for missing */
	public Integer getVisibility() {
		return latest_visibility;
	}

	/** Set the visibility in meters or null for missing */
	public void setVisibility(Integer v) {
		if(!integerEquals(v, latest_visibility)) {
			latest_visibility = v;
			notifyAttribute("visibility");
		}
	}

	/** Wind speed in KPH or null if missing */
	private transient Integer latest_wind_speed;

	/** Get the wind speed in kph or null if missing */
	public Integer getWindSpeed() {
		return latest_wind_speed;
	}

	/** Set the wind speed in kph or MISSING_DATA */
	public void setWindSpeed(Integer ws) {
		if(!integerEquals(ws, latest_wind_speed)) {
			latest_wind_speed = ws;
			notifyAttribute("windSpeed");
		}
	}

	/** Compare two (possibly-null) integers for equality */
	//FIXME: move to base class (DMSImpl also)
	static protected boolean integerEquals(Integer i0, Integer i1) {
		if(i0 == null)
			return i1 == null;
		else
			return i0.equals(i1);
	}

	/** The latest air temp in C or null for missing */
	private transient Integer latest_air_temp = null;

	/** Get the latest air temp in C or null if missing */
	public Integer getAirTemp() {
		return latest_air_temp;
	}

	/** Set the latest air temperature.
	 * @param at Air temperature in Celsius or null for missing */
	public void setAirTemp(Integer at) {
		if(!integerEquals(at, latest_air_temp)) {
			latest_air_temp = at;
			notifyAttribute("airTemp");
		}
	}

	/** Average wind direction in degrees or null for missing */
	private transient Integer latest_wind_dir;

	/** Get the latest average wind direction.
	 * @return Wind direction in degrees or null for missing */
	public Integer getWindDirAvg() {
		return latest_wind_dir;
	}

	/** Set the latest average wind direction.
	 * @param wd Wind direction in degrees or null for missing. This 
	 *	  angle is rounded to the nearest 10 degrees. */
	public void setWindDirAvg(Integer wd) {
		Integer a = new Angle(wd).round(10).toDegsInteger();
		if(!integerEquals(a, latest_wind_dir)) {
			latest_wind_dir = a;
			notifyAttribute("windDirAvg");
		}
	}

	/** Precipitation rate in mm/hr or null for missing */
	private transient Integer latest_precip_rate;

	/** Get the latest precipitation rate or null for missing */
	public Integer getPrecipRate() {
		return latest_precip_rate;
	}

	/** Set the latest precipitation rate in mm/hr or null for missing */
	public void setPrecipRate(Integer pr) {
		if(!integerEquals(pr, latest_precip_rate)) {
			latest_precip_rate = pr;
			notifyAttribute("precipRate");
		}
	}

	/** Store sensor values and update the sensor state. The current
	 * server time is used for the time stamp.
	 * @param v Visibility in meters or null for missing.
	 * @param ws Wind speed in kph or null for missing.
	 * @param at Air temp in C or null for missing.
	 * @param wd Wind direction in degrees or null for missing.
	 * @param pr Precipitation rate in mm/hr or null for missing. */
	public void store(Integer v, Integer ws, Integer at, Integer wd,
		Integer pr)
	{
		long now = TimeSteward.currentTimeMillis();
		setStamp(now);
		setVisibility(v);
		setWindSpeed(ws);
		setAirTemp(at);
		setWindDirAvg(wd);
		setPrecipRate(pr);
		updateState(now);
	}

	/** High wind state */
	private transient boolean high_wind = false;

	/** Get the high wind state */
	public boolean getHighWind() {
		return high_wind;
	}

	/** Set the high wind state */
	public void setHighWind(boolean hw) {
		if(hw != high_wind) {
			high_wind = hw;
			notifyAttribute("highWind");
		}
	}

	/** Low visibility state */
	private transient boolean low_visibility = false;

	/** Get the low visibility state */
	public boolean getLowVisibility() {
		return low_visibility;
	}

	/** Set the low visibility state */
	public void setLowVisibility(boolean lv) {
		if(lv != low_visibility) {
			low_visibility = lv;
			notifyAttribute("lowVisibility");
		}
	}

	/** Expired indicator */
	private transient boolean sample_expired = true;

	/** Get the expired state */
	public boolean getExpired() {
		return sample_expired;
	}

	/** Set the expired state */
	public void setExpired(boolean sr) {
		if(sr != sample_expired) {
			sample_expired = sr;
			notifyAttribute("expired");
		}
	}

	/** Update the sensor's state, which is a function of the current time 
	 * and most recent observations.
	 * @param now Time used to determine if last observation is too old. */
	public void updateState(long now) {
		updateExpired(now);
		setLowVisibility(isLowVisibility(now));
		setHighWind(isHighWind(now));
	}

	/** Is visibility low? Call method updateExpired first. */
	private boolean isLowVisibility(long now) {
		if(getExpired())
			return false;
		Length v = new Length(getVisibility());
		if(v.isMissing())
			return false;
		return v.toM() < WeatherSensorHelper.getLowVisLimitMeters();
	}

	/** Is wind speed high? Call method updateExpired first. */
	public boolean isHighWind(long now) {
		if(getExpired())
			return false;
		Integer s = getWindSpeed();
		if(s == null)
			return false;
		int t = WeatherSensorHelper.getHighWindLimitKph();
		int m = WeatherSensorHelper.getMaxValidWindSpeedKph();
		if(m <= 0)
			return s > t;
		else
			return s > t && s <= m;
	}

	/** Update the expired state using the specified time.
	 * @return True if the sample is expired else false. */
	private boolean updateExpired(long now) {
		boolean ex = isExpired(now);
		setExpired(ex);
		return ex;
	}

	/** Return true if the sample is expired, using the specified time. */
	private boolean isExpired(long now) {
		long limitsecs = WeatherSensorHelper.getObsAgeLimitSecs();
		if(limitsecs <= 0)
			return false;
		long stamp = getStamp();
		if(stamp != MISSING_DATA)
			return now - stamp > limitsecs * 1000;
		else
			return true;
	}

	/** Purge all samples before a given stamp. */
	public void purge(long before) {
		cache.purge(before);
		pt_cache.purge(before);
	}
}
