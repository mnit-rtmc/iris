/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
 * Copyright (C) 2018-2024  Minnesota Department of Transportation
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Gps;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.GpsPoller;

/**
 * Implements a GPS (Global Positioning System) device.
 *
 * @author John L. Stanley - SRF Consulting
 * @author Douglas Lau
 */
public class GpsImpl extends DeviceImpl implements Gps {

	/** GPS debug log */
	static public final DebugLog GPS_LOG = new DebugLog("gps");

	/** Get the jitter tolerance (m) */
	static private int jitterToleranceMeters() {
		return SystemAttrEnum.GPS_JITTER_M.getInt();
	}

	/** Save a location change to the geo_loc table
	 *  and update the GIS-info fields.
	 * @param loc GeoLocImpl to save to.
	 * @param lat Latitude.
	 * @param lon Longitude. */
	static private void changeDeviceLocation(GeoLocImpl loc, double lat,
		double lon)
	{
		try {
			loc.setLatNotify(lat);
			loc.setLonNotify(lon);
			loc.doCalculateGIS();
		}
		catch (TMSException ex) {
			GPS_LOG.log("Error updating geoloc: " + ex);
		}
	}

	/** Test for valid location */
	static private boolean isValidLocation(Double lt, Double ln) {
		return (lt != null) && (lt != 0.0)
		    && (ln != null) && (ln != 0.0);
	}

	/** Load all the GPS */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, controller, pin, notes, geo_loc, " +
			"latest_poll, latest_sample, lat, lon FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GpsImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("geo_loc", geo_loc);
		map.put("latest_poll", asTimestamp(latest_poll));
		map.put("latest_sample", asTimestamp(latest_sample));
		map.put("lat", lat);
		map.put("lon", lon);
		return map;
	}

	/** Create a GPS */
	private GpsImpl(ResultSet row) throws SQLException {
		this(row.getString(1),          // name
		     row.getString(2),          // controller
		     row.getInt(3),             // pin
		     row.getString(4),          // notes
		     row.getString(5),          // geo_loc
		     row.getTimestamp(6),       // latest_poll
		     row.getTimestamp(7),       // latest_sample
		     (Double) row.getObject(8), // lat
		     (Double) row.getObject(9)  // lon
		);
	}

	/** Create a GPS */
	private GpsImpl(String n, String c, int p, String nt, String loc,
	                Date lp, Date ls, Double lt, Double ln)
	{
		super(n, lookupController(c), p, nt);
		geo_loc = lookupGeoLoc(loc);
		latest_poll = stampMillis(lp);
		latest_sample = stampMillis(ls);
		lat = lt;
		lon = ln;
		initTransients();
	}

	/** Create a new GPS */
	public GpsImpl(String n) throws TMSException, SonarException {
		super(n);
	}

	/** Associated device location */
	private GeoLocImpl geo_loc;

	/** Set the associated device location */
	@Override
	public void setGeoLoc(GeoLoc loc) {
		if (loc instanceof GeoLocImpl)
			geo_loc = (GeoLocImpl) loc;
	}

	/** Set the associated device location */
	public void doSetGeoLoc(GeoLoc loc) throws TMSException {
		if (!objectEquals(loc, geo_loc)) {
			store.update(this, "geo_loc", loc);
			setGeoLoc(loc);
		}
	}

	/** Get the associated device location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Timestamp when latest poll was attempted */
	private Long latest_poll;

	/** Set the latest poll date &amp; time */
	public void setLatestPollNotify() {
		long dt = TimeSteward.currentTimeMillis();
		try {
			store.update(this, "latest_poll", asTimestamp(dt));
			latest_poll = dt;
			notifyAttribute("latestPoll");
		}
		catch (TMSException ex) {
			GPS_LOG.log("Error: " + ex);
		}
	}

	/** Get the latest poll date &amp; time */
	@Override
	public Long getLatestPoll() {
		return latest_poll;
	}

	/** Timestamp of most recent sample */
	private Long latest_sample;

	/** Set the latest sample date &amp; time */
	private void setLatestSampleNotify(Long dt) throws TMSException {
		if (!objectEquals(dt, latest_sample)) {
			store.update(this, "latest_sample", asTimestamp(dt));
			latest_sample = dt;
			notifyAttribute("latestSample");
		}
	}

	/** Get the latest sample date &amp; time */
	@Override
	public Long getLatestSample() {
		return latest_sample;
	}

	/** Latitude of most recent sample */
	private Double lat;

	/** Set the latitude */
	private void setLatNotify(Double lt) throws TMSException {
		if (!objectEquals(lt, lat)) {
			store.update(this, "lat", lat);
			lat = lt;
			notifyAttribute("lat");
		}
	}

	/** Get the most recent latitude */
	@Override
	public Double getLat() {
		return lat;
	}

	/** Longitude of most recent sample */
	private Double lon;

	/** Set the longitude */
	private void setLonNotify(Double ln) throws TMSException {
		if (!objectEquals(ln, lon)) {
			store.update(this, "lon", ln);
			lon = ln;
			notifyAttribute("lon");
		}
	}

	/** Get the most recent longitude */
	@Override
	public Double getLon() {
		return lon;
	}

	/** Save a device's lat/lon values and update the device's GIS info.
	 * @param loc Device location to update.
	 * @param lt New latitude.
	 * @param ln New longitude. */
	public void saveDeviceLocation(double lt, double ln) {
		GeoLocImpl loc = geo_loc;
		if (loc != null && isValidLocation(lt, ln)) {
			if (checkJitter(lt, ln)) {
				changeDeviceLocation(loc, lt, ln);
				updateLatLon(lt, ln);
			}
		}
	}

	/** Check if position has moved more than jitter threshold.
	 * @param lt New latitude.
	 * @param ln New longitude.
	 * @return true if new position should be stored. */
	private boolean checkJitter(double lt, double ln) {
		Double old_lat = geo_loc.getLat();
		Double old_lon = geo_loc.getLon();
		if (!isValidLocation(old_lat, old_lon))
			return true;
		Position p0 = new Position(old_lat, old_lon);
		Position p1 = new Position(lt, ln);
		return p0.distanceHaversine(p1) >= jitterToleranceMeters();
	}

	/** Save a location change to the _gps table.
	 * (Also updates the _gps.latest_sample field.)
	 * @param lt Latitude.
	 * @param ln Longitude. */
	private void updateLatLon(Double lt, Double ln) {
		try {
			setLatNotify(lt);
			setLonNotify(ln);
			if (lt != null && ln != null)
				setLatestSampleNotify(getLatestPoll());
		}
		catch (TMSException ex) {
			GPS_LOG.log("Error updating gps: " + ex);
		}
	}

	/** Request a device operation (query message, test pixels, etc.) */
	@Override
	public void sendDeviceRequest(DeviceRequest dr) {
		if (geo_loc != null) {
			GpsPoller p = getGpsPoller();
			if (p != null)
				p.sendRequest(this, dr);
		}
	}

	/** Get the GPS poller */
	private GpsPoller getGpsPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof GpsPoller) ? (GpsPoller) dp : null;
	}

	/** Perform a periodic poll */
	@Override
	public void periodicPoll(boolean is_long) {
		if (is_long)
			sendDeviceRequest(DeviceRequest.QUERY_GPS_LOCATION);
	}

	/** Request a device operation */
	@Override
	public void setDeviceRequest(int r) {
		DeviceRequest dr = DeviceRequest.fromOrdinal(r);
		// Clear lat/lon to defeat jitter filter (force update)
		if (DeviceRequest.QUERY_GPS_LOCATION == dr)
			updateLatLon(null, null);
		sendDeviceRequest(dr);
	}
}
