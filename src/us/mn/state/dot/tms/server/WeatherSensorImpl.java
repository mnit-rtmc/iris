/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.SQLException;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.PavementSurfaceStatus;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.utils.SString;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import static us.mn.state.dot.tms.server.XmlWriter.createAttribute;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.WeatherPoller;

/**
 * A weather sensor is a device for sampling weather data, such as 
 * precipitation rates, visibility, wind speed, etc. Weather sensor
 * drivers support:
 *   Optical Scientific ORG-815 optical rain gauge
 *   SSI CSV file interface
 *   Campbell Scientific CR1000 V27.05
 *   Vaisala dmc586 2.4.16
 *   QTT LX-RPU Elite Model Version 1.23
 *
 * @author Douglas Lau
 * @author Michael Darter
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
		if (!objectEquals(at, air_temp)) {
			air_temp = at;
			notifyAttribute("airTemp");
		}
	}

	/** Humidity as a percentage (null for missing) */
	private transient Integer humidity;

	/** Get the humidity as a percentage (null if missing) */
	@Override
	public Integer getHumidity() {
		return humidity;
	}

	/** Set the humidity.
	 * @param hu Humidity as a percentage or null for missing */
	public void setHumidityNotify(Integer hu) {
		if (!objectEquals(hu, humidity)) {
			humidity = hu;
			notifyAttribute("humidity");
		}
	}

	/** Dew point temperature in C (null for missing) */
	private transient Integer dew_point_temp;

	/** Get the dew point temp in C (null if missing) */
	@Override
	public Integer getDewPointTemp() {
		return dew_point_temp;
	}

	/** Set the dew point temp in C.
	 * @param dpt Dew point temperature in C (null for missing) */
	public void setDewPointTempNotify(Integer dp) {
		if (!objectEquals(dp, dew_point_temp)) {
			dew_point_temp = dp;
			notifyAttribute("dewPointTemp");
		}
	}

	/** Max temperature in C (null for missing) */
	private transient Integer max_temp;

	/** Get the max temperature in C (null if missing) */
	@Override
	public Integer getMaxTemp() {
		return max_temp;
	}

	/** Set the max temperature in C
	 * @param mt Max temperature in C (null for missing) */
	public void setMaxTempNotify(Integer mt) {
		if (!objectEquals(mt, max_temp)) {
			max_temp = mt;
			notifyAttribute("maxTemp");
		}
	}

	/** Min temperature in C (null for missing) */
	private transient Integer min_temp;

	/** Get the min temperature in C (null if missing) */
	@Override
	public Integer getMinTemp() {
		return min_temp;
	}

	/** Set the min temperature in C
	 * @param mt Min temperature in C (null for missing) */
	public void setMinTempNotify(Integer mt) {
		if (!objectEquals(mt, min_temp)) {
			min_temp = mt;
			notifyAttribute("minTemp");
		}
	}

	/** Wind speed in KPH (null if missing) */
	private transient Integer wind_speed;

	/** Get the wind speed in KPH (null if missing) */
	@Override
	public Integer getWindSpeed() {
		return wind_speed;
	}

	/** Set the average wind speed in KPH
	 * @param ws Wind speed in KPH or null if missing */
	public void setWindSpeedNotify(Integer ws) {
		if (!objectEquals(ws, wind_speed)) {
			wind_speed = ws;
			notifyAttribute("windSpeed");
		}
	}

	/** Max wind gust speed in KPH (null if missing) */
	private transient Integer max_wind_gust_speed;

	/** Get the max wind gust speed in KPH (null if missing) */
	@Override
	public Integer getMaxWindGustSpeed() {
		return max_wind_gust_speed;
	}

	/** Set the max wind gust speed in KPH
	 * @param ws Wind gust speed in KPH or null if missing */
	public void setMaxWindGustSpeedNotify(Integer ws) {
		if (!objectEquals(ws, max_wind_gust_speed)) {
			max_wind_gust_speed = ws;
			notifyAttribute("maxWindGustSpeed");
		}
	}

	/** Max wind gust direction in degress (null if missing) */
	private transient Integer max_wind_gust_dir;

	/** Get the max wind gust direction in degrees (null if missing) */
	@Override
	public Integer getMaxWindGustDir() {
		return max_wind_gust_dir;
	}

	/** Set the max wind gust direction in degrees
	 * @param ws Max wind gust direction in degress or null if missing */
	public void setMaxWindGustDirNotify(Integer wgd) {
		if (!objectEquals(wgd, max_wind_gust_dir)) {
			max_wind_gust_dir = wgd;
			notifyAttribute("maxWindGustDir");
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

	/** Set the average wind direction.
	 * @param wd Wind direction in degrees (null for missing) */
	public void setWindDirNotify(Integer wd) {
		if (!objectEquals(wd, wind_dir)) {
			wind_dir = wd;
			notifyAttribute("windDir");
		}
	}

	/** Set the average wind direction and round to the nearest 45 degs.
	 * @param wd Wind direction in degrees (null for missing) */
	public void setWindDirRoundNotify(Integer wd) {
		setWindDirNotify(round45(wd));
	}

	/** Spot wind direction in degrees (null for missing) */
	private transient Integer spot_wind_dir;

	/** Get spot wind direction.
	 * @return Spot wind direction in degrees (null for missing) */
	@Override
	public Integer getSpotWindDir() {
		return spot_wind_dir;
	}

	/** Set spot wind direction.
	 * @param swd Spot wind direction in degrees (null for missing) */
	public void setSpotWindDirNotify(Integer swd) {
		if (!objectEquals(swd, spot_wind_dir)) {
			spot_wind_dir = swd;
			notifyAttribute("spotWindDir");
		}
	}

	/** Spot wind speed in KPH (null for missing) */
	private transient Integer spot_wind_speed;

	/** Get spot wind speed.
	 * @return Spot wind speed in degrees (null for missing) */
	@Override
	public Integer getSpotWindSpeed() {
		return spot_wind_speed;
	}

	/** Set spot wind speed.
	 * @param swd Spot wind speed in KPH (null for missing) */
	public void setSpotWindSpeedNotify(Integer sws) {
		if (!objectEquals(sws, spot_wind_speed)) {
			spot_wind_speed = sws;
			notifyAttribute("spotWindSpeed");
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
		if (stamp != null && now >= stamp) {
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
	public void setPrecipRateNotify(Integer pr) {
		if (!objectEquals(pr, precip_rate)) {
			precip_rate = pr;
			notifyAttribute("precipRate");
		}
	}

	/** Set the type of precipitation */
	public void setPrecipitationType(PrecipitationType pt, long st) {
		pt_cache.add(new PeriodicSample(st, SAMPLE_PERIOD_SEC,
			pt.ordinal()));
	}

	/** Precipitation situation (null for missing) */
	private transient Integer precip_situation;

	/** Get precipitation situation (null for missing) */
	@Override
	public Integer getPrecipSituation() {
		return precip_situation;
	}

	/** Set precipitation situation (null for missing) */
	public void setPrecipSituationNotify(Integer prs) {
		if (!objectEquals(prs, precip_situation)) {
			precip_situation = prs;
			notifyAttribute("precipSituation");
		}
	}

	/** Precipitation accumulation 1h in mm (null for missing) */
	private transient Integer precip_one_hour;

	/** Get precipitation 1h in mm (null for missing) */
	@Override
	public Integer getPrecipOneHour() {
		return precip_one_hour;
	}

	/** Set precipitation 1h in mm (null for missing) */
	public void setPrecipOneHourNotify(Integer pr) {
		if (!objectEquals(pr, precip_one_hour)) {
			precip_one_hour = pr;
			notifyAttribute("precipOneHour");
		}
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
		if (!objectEquals(v, visibility_m)) {
			visibility_m = v;
			notifyAttribute("visibility");
		}
	}

	/** Atmospheric pressure in pascals (null for missing) */
	private transient Integer pressure;

	/** Get atmospheric pressure in pascals (null for missing) */
	@Override
	public Integer getPressure() {
		return pressure;
	}

	/** Set atmospheric pressure in pascals (null for missing) */
	public void setPressureNotify(Integer v) {
		if (!objectEquals(v, pressure)) {
			pressure = v;
			notifyAttribute("pressure");
		}
	}

	/** Pavement surface temperature (null for missing) */
	private transient Integer pvmt_surf_temp;

	/** Get pavement surface temperature (null for missing) */
	@Override
	public Integer getPvmtSurfTemp() {
		return pvmt_surf_temp;
	}

	/** Set pavement surface temperature (null for missing) */
	public void setPvmtSurfTempNotify(Integer v) {
		if (!objectEquals(v, pvmt_surf_temp)) {
			pvmt_surf_temp = v;
			notifyAttribute("pvmtSurfTemp");
		}
	}

	/** Surface temperature (null for missing) */
	private transient Integer surf_temp;

	/** Get surface temperature (null for missing) */
	@Override
	public Integer getSurfTemp() {
		return surf_temp;
	}

	/** Set surface temperature (null for missing) */
	public void setSurfTempNotify(Integer v) {
		if (!objectEquals(v, surf_temp)) {
			surf_temp = v;
			notifyAttribute("SurfTemp");
		}
	}

	/** Pavement surface status (null for missing) */
	private transient Integer pvmt_surf_status;

	/** Get pavement surface status (null for missing) */
	@Override
	public Integer getPvmtSurfStatus() {
		return pvmt_surf_status;
	}

	/** Set pavement surface status (null for missing) */
	public void setPvmtSurfStatusNotify(Integer v) {
		if (!objectEquals(v, pvmt_surf_status)) {
			pvmt_surf_status = v;
			notifyAttribute("pvmtSurfStatus");
		}
	}

	/** Pavement surface freeze point (null for missing) */
	private transient Integer surf_freeze_temp;

	/** Get pavement surface freeze temp (null for missing) */
	@Override
	public Integer getSurfFreezeTemp() {
		return surf_freeze_temp;
	}

	/** Set pavement surface freeze temperature (null for missing) */
	public void setSurfFreezeTempNotify(Integer v) {
		if (!objectEquals(v, surf_freeze_temp)) {
			surf_freeze_temp = v;
			notifyAttribute("surf_freeze_temp");
		}
	}

	/** Pavement subsurface temperature (null for missing) */
	private transient Integer subsurf_temp;

	/** Get subsurface temp (null for missing) */
	@Override
	public Integer getSubSurfTemp() {
		return subsurf_temp;
	}

	/** Set subsurface temperature (null for missing) */
	public void setSubSurfTempNotify(Integer v) {
		if (!objectEquals(v, subsurf_temp)) {
			subsurf_temp = v;
			notifyAttribute("subsurf_temp");
		}
	}

	/** Time stamp from the last sample */
	private transient Long stamp;

	/** Get the time stamp from the last sample.
	 * @return Time as long */
	@Override
	public Long getStamp() {
		return stamp;
	}

	/** Get the time stamp as a string */
	public String getStampString() {
		Long ts = getStamp();
		return (ts != null ? new Date(ts).toString() : "");
	}

	/** Set the time stamp for the current sample */
	public void setStampNotify(Long s) {
		stamp = s;
		notifyAttribute("stamp");
	}

	/** Get a weather sensor poller */
	private WeatherPoller getWeatherPoller() {
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

	/** Check if the sign is periodically queriable */
	public boolean isPeriodicallyQueriable() {
		return isConnected() || !hasModemCommLink();
	}

	/** Get a string representation of the object */
	public String toStringDebug() {
		StringBuilder sb = new StringBuilder();
		sb.append("(WeatherSensor: name=").append(name);
		sb.append(" time_stamp=").append(getStampString());
		sb.append(" airTemp_c=").append(getAirTemp());
		sb.append(" dewPointTemp_c=").append(getDewPointTemp());
		sb.append(" maxTemp_c=").append(getMaxTemp());
		sb.append(" minTemp_c=").append(getMinTemp());
		sb.append(" avgWindSpeed_kph=").append(getWindSpeed());
		sb.append(" avgWindDir_degs=").append(getWindDir());
		sb.append(" maxWindGustSpeed_kph=").
			append(getMaxWindGustSpeed());
		sb.append(" maxWindGustDir_degs=").append(getMaxWindGustDir());
		sb.append(" spotWindDir_degs=").append(getSpotWindDir());
		sb.append(" spotWindSpeed_kph=").append(getSpotWindSpeed());
		sb.append(" precip_rate_mmhr=").append(getPrecipRate());
		sb.append(" precip_situation=").append(getPrecipSituation());
		sb.append(" precip_1h_mm=").append(getPrecipOneHour());
		sb.append(" visibility_m=").append(getVisibility());
		sb.append(" humidity_perc=").append(getHumidity());
		sb.append(" atmos_pressure_pa=").append(getPressure());
		sb.append(" pvmt_surf_temp_c=").append(getPvmtSurfTemp());
		sb.append(" surf_temp_c=").append(getSurfTemp());
		sb.append(" pvmt_surf_status=").append(
			WeatherSensorHelper.getPvmtSurfStatus(
			(WeatherSensor)this));
		sb.append(" surf_freeze_temp_c=").append(getSurfFreezeTemp());
		sb.append(" subsurf_temp_c=").append(getSubSurfTemp());
		sb.append(")");
		return sb.toString();
	}

	/** Write object as xml */
	public void writeWeatherSensorXml(Writer w) throws IOException {
		w.write("<weather_sensor");
		w.write(createAttribute("name", getName()));
		w.write(createAttribute("description",
			GeoLocHelper.getDescription(geo_loc)));
		Position pos = GeoLocHelper.getWgs84Position(geo_loc);
		if (pos != null) {
			w.write(createAttribute("lon",
				formatDouble(pos.getLongitude())));
			w.write(createAttribute("lat",
				formatDouble(pos.getLatitude())));
		}
		w.write(createAttribute("air_temp_c", getAirTemp()));
		w.write(createAttribute("humidity_perc", getHumidity()));
		w.write(createAttribute("dew_point_temp_c", 
			getDewPointTemp()));
		w.write(createAttribute("max_temp_c", getMaxTemp()));
		w.write(createAttribute("min_temp_c", getMinTemp()));
		w.write(createAttribute("avg_wind_speed_kph", getWindSpeed()));
		w.write(createAttribute("max_wind_gust_speed_kph", 
			getMaxWindGustSpeed()));
		w.write(createAttribute("max_wind_gust_dir_degs", 
			getMaxWindGustDir()));
		w.write(createAttribute("avg_wind_dir_degs", getWindDir()));
		w.write(createAttribute("spot_wind_speed_kph", 
			getSpotWindSpeed()));
		w.write(createAttribute("spot_wind_dir_degs", 
			getSpotWindDir()));
		w.write(createAttribute("precip_rate_mmhr", getPrecipRate()));
		w.write(createAttribute("precip_situation", 
			getPrecipSituation()));
		w.write(createAttribute("precip_1h_mm", getPrecipOneHour()));
		w.write(createAttribute("visibility_m", getVisibility()));
		w.write(createAttribute("atmos_pressure_pa", getPressure()));
		w.write(createAttribute("pvmt_surf_temp_c", 
			getPvmtSurfTemp()));
		w.write(createAttribute("surf_temp_c", 
			getSurfTemp()));
		w.write(createAttribute("pvmt_surf_status=", 
			WeatherSensorHelper.getPvmtSurfStatus(
			(WeatherSensor)this)));
		w.write(createAttribute("surf_freeze_temp_c", 
			getSurfFreezeTemp()));
		w.write(createAttribute("subsurf_temp_c", 
			getSubSurfTemp()));
		w.write(createAttribute("time_stamp", getStampString()));
		w.write("/>\n");
	}
}
