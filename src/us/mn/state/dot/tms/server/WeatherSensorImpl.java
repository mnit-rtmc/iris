/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2022  Minnesota Department of Transportation
 * Copyright (C) 2017-2021  Iteris Inc.
 * Copyright (C) 2023-2024  SRF Consulting Group
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.sql.ResultSet;
import java.sql.SQLException;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.WeatherSensor;
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
 * @author Gordon Parikh
 * @author John L. Stanley
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
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"site_id, alt_id FROM iris." + SONAR_TYPE + ";", 
			new ResultFactory()
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
		map.put("site_id", site_id);
		map.put("alt_id", alt_id);
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
		     row.getString(5),		// notes
		     row.getString(6),		// site_id
		     row.getString(7)		// alt_id
		);
	}

	/** Create a weather sensor */
	private WeatherSensorImpl(String n, String l, String c, int p,
		String nt, String sid, String aid)
	{
		super(n, lookupController(c), p, nt);
		site_id = sid;
		alt_id = aid;
		geo_loc = lookupGeoLoc(l);
		cache = new PeriodicSampleCache(PeriodicSampleType.PRECIP_RATE);
		pt_cache = new PeriodicSampleCache(
			PeriodicSampleType.PRECIP_TYPE);
		settings = null;
		initTransients();
	}

	/** Create a new weather sensor with a string name */
	public WeatherSensorImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name, SONAR_TYPE);
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

	/** Site id (null for missing) */
	private transient String site_id;

	/** Get the site id (null for missing) */
	@Override
	public String getSiteId() {
		return site_id;
	}

	/** Set the site id.
	 * @param sid Site id (null for missing) */
	public void setSiteId(String sid) {
		site_id = sid;
	}

	/** Set the site id.
	 * @param sid Site id (null for missing) */
	public void doSetSiteId(String sid) throws TMSException {
		if (!objectEquals(sid, site_id)) {
			store.update(this, "site_id", sid);
			setSiteId(sid);
		}
	}

	/** Alt id (null for missing) */
	private transient String alt_id;

	/** Get the alt id (null for missing) */
	@Override
	public String getAltId() {
		return alt_id;
	}

	/** Set the alt id.
 	 * @param aid Alt id (null for missing) */
	public void setAltId(String aid) {
		alt_id = aid;
	}

	/** Set the alt id.
	 * @param aid (null for missing) */
	public void doSetAltId(String aid) throws TMSException {
		if (!objectEquals(aid, alt_id)) {
			store.update(this, "alt_id", aid);
			setAltId(aid);
		}
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
	 * @param dp Dew point temperature in C (null for missing) */
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
		return getTestOverride("max.wind.gust.speed", max_wind_gust_speed);
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
	 * @param wgd Max wind gust direction in degress or null if missing */
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
	 * @param sws Spot wind speed in KPH (null for missing) */
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
		int per_sec = calculatePeriod(st);
		int value = calculatePrecipValue(a);
		if (per_sec > 0 && value >= 0) {
			cache.add(new PeriodicSample(st, per_sec, value), name);
			float per_h = 3600f / per_sec;  // periods per hour
			float umph = value * per_h;     // micrometers per hour
			float mmph = umph / 1000;       // millimeters per hour
			setPrecipRateNotify(Math.round(mmph));
		}
		if (value < 0)
			setPrecipRateNotify(null);
		if (per_sec > 0 || value < 0)
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
			pt.ordinal()), name);
	}

	/** Precipitation situation (null for missing) */
	private transient String precip_situation;

	/** Get precipitation situation (null for missing) */
	@Override
	public String getPrecipSituation() {
		return precip_situation;
	}

	/** Set precipitation situation (null for missing) */
	public void setPrecipSituationNotify(String prs) {
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
		return getTestOverride("visibility.m", visibility_m);
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
		return getTestOverride("surf.temp", surf_temp);
	}

	/** Set surface temperature (null for missing) */
	public void setSurfTempNotify(Integer v) {
		if (!objectEquals(v, surf_temp)) {
			surf_temp = v;
			notifyAttribute("surfTemp");
		}
	}

	/** Pavement surface status (null for missing) */
	private transient String pvmt_surf_status;

	/** Get pavement surface status (null for missing) */
	@Override
	public String getPvmtSurfStatus() {
		return pvmt_surf_status;
	}

	/** Set pavement surface status (null for missing) */
	public void setPvmtSurfStatusNotify(String v) {
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
			notifyAttribute("surfFreezeTemp");
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
			notifyAttribute("subSurfTemp");
		}
	}

	/** Pavement friction (null for missing) */
	private transient Integer pvmt_friction;

	/** Get pavement friction (null for missing) */
	@Override
	public Integer getPvmtFriction() {
		return getTestOverride("pvmt.friction", pvmt_friction);
	}

	/** Set pavement friction (null for missing) */
	public void setPvmtFrictionNotify(Integer v) {
		if (!objectEquals(v, pvmt_friction)) {
			pvmt_friction = v;
			notifyAttribute("pvmtFriction");
		}
	}

	/** Surface conductivity (V2) (null for missing) */
	private transient Integer surface_conductivity_v2;

	/** Get surface conductivity (V2) (null for missing) */
	@Override
	public Integer getSurfCondV2() {
		return surface_conductivity_v2;
	}

	/** Set surface conductivity (V2) (null for missing) */
	public void setSurfCondV2Notify(Integer v) {
		if (!objectEquals(v, surface_conductivity_v2)) {
			surface_conductivity_v2 = v;
			notifyAttribute("surfCondV2");
		}
	}

	/** Total sun int in terms of minutes */
	private transient Integer total_sun;

	/** Get total sun int in terms of minutes */
	@Override
	public Integer getTotalSun() {
		return total_sun;
	}

	/** Set total sun int in terms of minutes */
	public void setTotalSunNotify(Integer ts) {
		if (!objectEquals(ts, total_sun)) {
			total_sun = ts;
			notifyAttribute("totalSun");
		}
	}

	/** instantaneousSolar int in terms of W/m^2 */
	private transient Integer instantaneous_solar;

	/** Get instantaneousSolar int in terms of W/m^2 */
	@Override
	public Integer getInstantaneousSolar() {
		return instantaneous_solar;
	}

	/** Set instantaneousSolar int in terms of W/m^2 */
	public void setInstantaneousSolarNotify(Integer is) {
		if (!objectEquals(is, instantaneous_solar)) {
			instantaneous_solar = is;
			notifyAttribute("instantaneousSolar");
		}
	}

	/** instantaneousTerrestrial object in terms of W/m^2 */
	private transient Integer instantaneous_terrestrial;

	/** Get instantaneousTerrestrial object in terms of W/m^2 */
	@Override
	public Integer getInstantaneousTerrestrial() {
		return instantaneous_terrestrial;
	}

	/** Set instantaneousTerrestrial object in terms of W/m^2 */
	public void setInstantaneousTerrestrialNotify(Integer it) {
		if (!objectEquals(it, instantaneous_terrestrial)) {
			instantaneous_terrestrial = it;
			notifyAttribute("instantaneousTerrestrial");
		}
	}

	/** total radiation object in terms of W/m^2 */
	private transient Integer total_radiation;

	/** Get total radiation object in terms of W/m^2 */
	@Override
	public Integer getTotalRadiation() {
		return total_radiation;
	}

	/** Set total radiation object in terms of W/m^2 */
	public void setTotalRadiationNotify(Integer tr) {
		if (!objectEquals(tr, total_radiation)) {
			total_radiation = tr;
			notifyAttribute("totalRadiation");
		}
	}

	/** total radiation period int in terms of seconds */
	private transient Integer total_radiation_period;

	/** Get total radiation period int in terms of seconds */
	@Override
	public Integer getTotalRadiationPeriod() {
		return total_radiation_period;
	}

	/** Set total radiation period int in terms of seconds */
	public void setTotalRadiationPeriodNotify(Integer trp) {
		if (!objectEquals(trp, total_radiation_period)) {
			total_radiation_period = trp;
			notifyAttribute("totalRadiationPeriod");
		}
	}

	/** total solar radiation period int in terms of J/m^2 */
	private transient Integer solar_radiation;

	/** Get total solar radiation period int in terms of J/m^2 */
	@Override
	public Integer getSolarRadiation() {
		return solar_radiation;
	}

	/** Set total solar radiation period int in terms of J/m^2 */
	public void setSolarRadiationNotify(Integer sr) {
		if (!objectEquals(sr, solar_radiation)) {
			solar_radiation = sr;
			notifyAttribute("solarRadiation");
		}
	}

	/** Settings (JSON) read from sensors */
	private String settings;

	/** Set the JSON settings */
	public void setSettings(String s) {
		if (!objectEquals(s, settings)) {
			try {
				store.update(this, "settings", s);
				settings = s;
			}
			catch (TMSException e) {
				logError("settings: " + e.getMessage());
			}
		}
	}

	/** Set the current JSON sample */
	public void setSample(String s) {
		try {
			store.update(this, "sample", s);
		}
		catch (TMSException e) {
			logError("sample: " + e.getMessage());
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
		try {
			store.update(this, "sample_time", asTimestamp(s));
			stamp = s;
			notifyAttribute("stamp");
		}
		catch (TMSException e) {
			// FIXME: what else can we do with this exception?
			e.printStackTrace();
		}
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

	/** Perform a periodic poll */
	@Override
	public void periodicPoll(boolean is_long) {
		if (!is_long)
			sendDeviceRequest(DeviceRequest.QUERY_STATUS);
	}

	/** Flush buffered sample data to disk */
	public void flush(PeriodicSampleWriter writer) {
		writer.flush(cache, name);
		writer.flush(pt_cache, name);
	}

	/** Purge all samples before a given stamp. */
	public void purge(long before) {
		cache.purge(before);
		pt_cache.purge(before);
	}

	/** Get a string representation of the object */
	public String toStringDebug() {
		StringBuilder sb = new StringBuilder();
		sb.append("(WeatherSensor: name=").append(name);
		sb.append(" time_stamp=").append(getStampString());
		sb.append(" siteId=").append(getSiteId());
		sb.append(" altId=").append(getAltId());
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
		sb.append(" pvmt_surf_status=").append(getPvmtSurfStatus());
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
			GeoLocHelper.getLocation(geo_loc)));
		Position pos = GeoLocHelper.getWgs84Position(geo_loc);
		if (pos != null) {
			w.write(createAttribute("lon",
				formatDouble(pos.getLongitude())));
			w.write(createAttribute("lat",
				formatDouble(pos.getLatitude())));
		}
		w.write(createAttribute("site_id", getSiteId()));
		w.write(createAttribute("alt_id", getAltId()));
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
		w.write(createAttribute("surf_temp_c", getSurfTemp()));
		w.write(createAttribute("pvmt_surf_status=", 
			getPvmtSurfStatus()));
		w.write(createAttribute("surf_freeze_temp_c", 
			getSurfFreezeTemp()));
		w.write(createAttribute("subsurf_temp_c", 
			getSubSurfTemp()));
		w.write(createAttribute("time_stamp", getStampString()));
		w.write("/>\n");
	}

	/** Name of test properties file */
	static final String testFilename = "./ess_test.properties";
	
	/** Loaded copy of test properties */
	static private Properties testProp = null;
	
	/** Map of prefix values for each device in test file*/
	static private HashMap<String,String> testPrefix =
			new HashMap<String,String>();
	
	/** Load ESS test properties.
	 * Called at start of each ESS update cycle. 
	 * This initially deletes the test file.
	 * On subsequent calls, it loads the test file.
	 * (So only a test properties file created
	 *  while the system is running has any effect.) */
	static public void loadTestProperties() {
		try {
			// On first call, delete test properties file...
			if (testProp == null) { 
				testProp = new Properties();
				File testFile = new File(testFilename);
				if (testFile.exists())
					testFile.delete();
				return;
			}
			// On subsequent calls, load test properties file...
			testPrefix.clear();
			testProp.clear();
			try (InputStream in =
					new FileInputStream(testFilename)) {
				testProp.load(in);
			} catch (FileNotFoundException ex) {
				return;
			}
			// Save key-prefix for each device name...
			for (String key: testProp.stringPropertyNames()) {
				if (key.matches("device\\d+\\.name")) {
					String devName = testProp.getProperty(key);
					String prefix = key.split("\\.")[0];
					testPrefix.put(devName, prefix);
				}
			}
		} catch (IOException|SecurityException ex) {
			System.err.println("ESS test file error:");
			ex.printStackTrace();
			// Next two lines disables test mode for this cycle.
			testPrefix.clear();
			testProp.clear();
		}
	}

	/** Get test override value */
	private Integer getTestOverride(String sensorName, Integer iDefault) {
		String prefix = testPrefix.get(getName());
		if (prefix != null) {
			String key = prefix + "." + sensorName;
			String sValue = (String) testProp.get(key);
			if (!SString.isBlank(sValue)) {
				if (sValue.equals("null"))
					return null;
				try {
					return Integer.valueOf(sValue);
				}
				catch (NumberFormatException ex) {
					System.err.println("ESS test value error: "+key+"="+sValue);
					ex.printStackTrace();
				}
			}
		}
		return iDefault;
	}
}
