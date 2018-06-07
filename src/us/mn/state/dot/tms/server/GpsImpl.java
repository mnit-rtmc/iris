/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
 * Copyright (C) 2018  Minnesota Department of Transportation
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
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Gps;
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

	/** Given a device name, lookup the associated GeoLocImpl (if there is
	 * one).
	 *
	 * @param dev_nm Name of device.
	 * @return The associated GeoLocImpl or null.
	 */
	static private GeoLocImpl lookupGeoLocImplForDevice(String dev_nm) {
		GeoLoc loc = GeoLocHelper.lookup(dev_nm);
		return (loc instanceof GeoLocImpl) ? (GeoLocImpl) loc : null;
	}

	/** Save a location change to the geo_loc table
	 *  and update the GIS-info fields.
	 * @param loc  GeoLocImpl to save to.
	 * @param lat  Latitude.
	 * @param lon  Longitude. */
	static private void changeDeviceLocation(GeoLocImpl loc, double lat,
		double lon)
	{
		if (isValidLocation(lat, lon)) {
			try {
				loc.setLatNotify(lat);
				loc.setLonNotify(lon);
				loc.doCalculateGIS();
			}
			catch (TMSException ex) {
				GPS_LOG.log("Error updating geoloc record: "+ex);
			}
		}
	}

	/** Test for valid location */
	static private boolean isValidLocation(double lat, double lon) {
		return (lat != 0.0) && (lon != 0.0);
	}

	/** Load all the GPS */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, GpsImpl.class);
		store.query("SELECT name, controller, pin, gps_enable, " +
			"device_name, device_class, poll_datetime, " +
			"sample_datetime, sample_lat, sample_lon, " +
			"comm_status, error_status, jitter_tolerance_meters " +
			"FROM iris." + SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GpsImpl(
					row.getString(1),	// name
					row.getString(2),	// controller
					row.getInt(3),		// pin
					row.getBoolean(4),	// gps_enable
					row.getString(5),	// device_name
					row.getString(6),	// device_class
					row.getTimestamp(7),// poll datetime
					row.getTimestamp(8),// sample_datetime
					row.getDouble(9),	// sample_lat
					row.getDouble(10),	// sample_lon
					row.getString(11),	// comm_status
					row.getString(12),	// error_status
					row.getInt(13)		// jitter_meters
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("gps_enable", gps_enable);
		map.put("device_name", device_name);
		map.put("device_class", device_class);
		map.put("poll_datetime", asTimestamp(pollDatetime));
		map.put("sample_datetime", asTimestamp(sampleDatetime));
		map.put("sample_lat", sample_lat);
		map.put("sample_lon", sample_lon);
		map.put("comm_status", comm_status);
		map.put("error_status", error_status);
		map.put("jitter_tolerance_meters", jitter_tolerance_meters);
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

	/** Create a GPS */
	public GpsImpl(String xname, ControllerImpl c, int p,
			boolean xgps_enable,
			String xdevice_name, String xdevice_class,
			Long xPollDatetime, Long xSampleDatetime,
			double xsample_lat, double xsample_lon,
			String xcomm_status, String xerror_status,
			int xjitter_tolerance_meters)
	{
		super(xname, c, p, "");
		gps_enable = xgps_enable;
		device_name = xdevice_name;
		device_class = xdevice_class;
		pollDatetime = xPollDatetime;
		sampleDatetime = xSampleDatetime;
		sample_lat = xsample_lat;
		sample_lon = xsample_lon;
		comm_status = xcomm_status;
		error_status = xerror_status;
		jitter_tolerance_meters = xjitter_tolerance_meters;
		initTransients();
	}

	/** Create a GPS */
	public GpsImpl(String xname, String sController, int p,
	               boolean xgps_enable, String xdevice_name,
	               String xdevice_class, Timestamp xtsPoll,
	               Timestamp xtsSample, double xsample_lat,
	               double xsample_lon, String xcomm_status,
	               String xerror_status, int xjitter_tolerance_meters)
	{
		this(xname, lookupController(sController), p, xgps_enable,
		     xdevice_name, xdevice_class, safeGetTime(xtsPoll),
		     safeGetTime(xtsSample), xsample_lat, xsample_lon,
		     xcomm_status, xerror_status, xjitter_tolerance_meters);
	}

	static private Long safeGetTime(Timestamp ts) {
		return (ts == null) ? 0 : ts.getTime();
	}

	public GpsImpl(String xname) throws TMSException, SonarException {
		super(xname);
		gps_enable = true;
		device_name = xname.substring(0, xname.length() - 4);
		device_class = "DMS";
		pollDatetime = null;
		sampleDatetime = null;
		sample_lat = new Double(0.0);
		sample_lon = new Double(0.0);
		comm_status = null;
		error_status = null;
		jitter_tolerance_meters = 100;
	}

	/** Save a device's lat/long values and update the device's GIS info.
	 *
	 * @param lat    New latitude.
	 * @param lon    New longitude.
	 * @param force  If true, override jitter filter.
	 */
	public void saveDeviceLocation(double lat, double lon, boolean force) {
		GeoLocImpl loc = lookupGeoLocImplForDevice(device_name);
		if (loc != null && (force || checkJitter(lat, lon)))
			changeDeviceLocation(loc, lat, lon);
		updateLatLon(lat, lon);
	}

	/** Check if position has moved more than jitter threshold.
	 * @param lat New latitude.
	 * @param lon New longitude.
	 * @return true if new position should be stored. */
	private boolean checkJitter(double lat, double lon) {
		double old_lat = getSampleLat();
		double old_lon = getSampleLon();
		if (!isValidLocation(old_lat, old_lon))
			return true;
		Position p0 = new Position(old_lat, old_lon);
		Position p1 = new Position(lat, lon);
		return p0.distanceHaversine(p1) >= jitter_tolerance_meters;
	}

	/** Save a location change to the _gps table.
	 * (Also updates the _gps.sample_datetime field.)
	 * @param lat Latitude.
	 * @param lon Longitude. */
	private void updateLatLon(double lat, double lon) {
		if (isValidLocation(lat, lon)) {
			try {
				setSampleLatNotify(lat);
				setSampleLonNotify(lon);
				setSampleDatetimeNotify(getPollDatetime());
				setCommStatusNotify("Done");
			}
			catch (TMSException ex) {
				GPS_LOG.log("Error updating gps record: " + ex);
			}
		}
	}

	/** Request a device operation (query message, test pixels, etc.) */
	@Override
	public void sendDeviceRequest(DeviceRequest dr) {
		GpsPoller p = getGpsPoller();
		if (p != null)
			p.sendRequest(this, dr);
	}

	/** Get the GPS poller */
	private GpsPoller getGpsPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof GpsPoller) ? (GpsPoller) dp : null;
	}

	protected void logError(String msg) {
		if (GPS_LOG.isOpen())
			GPS_LOG.log(getName() + ": " + msg);
	}

	public CommLink getCommLink() {
		Controller c = controller;
		return (c != null) ? c.getCommLink() : null;
	}

	//-----------------------------------------
	// The following methods manage the partially-merged
	// gps.gps_enable and comm_link.poll_enabled flags.

	/** Enable/disable GPS functions. */
	private boolean	gps_enable;

	/** Set the GPS enable flag */
	@Override
	public void setGpsEnable(boolean agps_enable) {
		gps_enable = agps_enable;
	}

	/** Set the GPS enable flag */
	public void doSetGpsEnable(boolean agps_enable) throws TMSException {
		if (agps_enable != gps_enable)
			store.update(this, "gps_enable", agps_enable);
		setGpsEnable(agps_enable);
		CommLink cl = getCommLink();
		if (cl != null)
			cl.setPollEnabled(agps_enable);
	}

	/** Get the GPS enable flag */
	@Override
	public boolean getGpsEnable() {
		CommLink cl = getCommLink();
		if (cl != null)
			gps_enable = cl.getPollEnabled();
		return gps_enable;
	}

	/** Name of device this gps is linked to.  Used
	 * for lookup in respective device table(s) and
	 * the geo_loc table. */
	private String device_name;

	/** Set the primary device name */
	@Override
	public void setDeviceName(String adevice_name) {
		device_name = adevice_name;
	}

	/** Set the primary device name */
	public void doSetDeviceName(String adevice_name) throws TMSException {
		if (adevice_name != device_name) {
			store.update(this, "device_name", adevice_name);
			setDeviceName(adevice_name);
		}
	}

	/** Get the primary device name */
	@Override
	public String getDeviceName() {
		return device_name;
	}

	/** Class of device GPS is linked to.
	 * [currently only "DMS"] */
	private String device_class;

	/** Set the primary device class */
	@Override
	public void setDeviceClass(String adevice_class) {
		device_class = adevice_class;
	}

	/** Set the primary device class */
	public void doSetDeviceClass(String adevice_class) throws TMSException {
		if (adevice_class != device_class) {
			store.update(this, "device_class", adevice_class);
			setDeviceClass(adevice_class);
		}
	}

	/** Get the primary device class */
	@Override
	public String getDeviceClass() {
		return device_class;
	}

	/** Timestamp when latest poll was attempted.
	 * 	Zero = never. */
	private Long pollDatetime;

	/** Set the last polled date & time */
	public void setPollDatetimeNotify(Long dt) {
		if ((dt != null) && !dt.equals(pollDatetime)) {
			storeUpdate("poll_datetime", asTimestamp(dt));
			pollDatetime = dt;
			notifyAttribute("pollDatetime");
		}
	}

	/** Get the last polled date & time */
	@Override
	public Long getPollDatetime() {
		return pollDatetime;
	}

	/** Timestamp of most recent sample.  Note that due
	 * to comm delays/problems, this may be different
	 * than poll_cycle_datetime.  Zero = never. */
	private Long sampleDatetime;

	/** Set the last successful poll date &amp; time */
	private void setSampleDatetimeNotify(Long dt) throws TMSException {
		if (!dt.equals(sampleDatetime)) {
			store.update(this, "sample_datetime", asTimestamp(dt));
			sampleDatetime = dt;
			notifyAttribute("sampleDatetime");
		}
	}

	/** Get the last successful poll date & time */
	@Override
	public Long getSampleDatetime() {
		return sampleDatetime;
	}

	/** Latitude of most recent sample. */
	private double sample_lat;

	/** Set the most recent latitude */
	private void setSampleLatNotify(double asample_lat) throws TMSException{
		if (asample_lat != sample_lat) {
			store.update(this, "sample_lat", asample_lat);
			sample_lat = asample_lat;
			notifyAttribute("sampleLat");
		}
	}

	/** Get the most recent latitude */
	@Override
	public double getSampleLat() {
		return sample_lat;
	}

	/** Longitude of most recent sample. */
	private double sample_lon;

	/** Set the most recent longitude */
	private void setSampleLonNotify(double asample_lon) throws TMSException{
		if (asample_lon != sample_lon) {
			store.update(this, "sample_lon", asample_lon);
			sample_lon = asample_lon;
			notifyAttribute("sampleLon");
		}
	}

	/** Get the most recent longitude */
	@Override
	public double getSampleLon() {
		return sample_lon;
	}

	/** Status of latest attempt to poll GPS.
	 * [usually "Done", "Polling", or "Error"] */
	private String comm_status;

	/** Set the comm status */
	public void setCommStatusNotify(String acomm_status) {
		if (acomm_status != comm_status) {
			storeUpdate("comm_status", acomm_status);
			comm_status = acomm_status;
			notifyAttribute("commStatus");
		}
	}

	/** Get the comm status */
	@Override
	public String getCommStatus() {
		return comm_status;
	}

	/** Error status.  Normally blank.  If comm_status
	 *  is set to "Error", this will contain a short
	 *  explanation of the error. */
	private String error_status;

	/** Set the error status */
	public void setErrorStatusNotify(String aerror_status) {
		if (aerror_status != null) {
			if (aerror_status.length() > 25)
				aerror_status = aerror_status.substring(0, 25);
			if (aerror_status != error_status) {
				storeUpdate("error_status", aerror_status);
				error_status = aerror_status;
				notifyAttribute("errorStatus");
			}
		}
	}

	/** Get the error status */
	@Override
	public String getErrorStatus() {
		return error_status;
	}

	/** Distance GPS must move before geo_loc
	 * location is changed. */
	private int jitter_tolerance_meters;

	/** Set the jitter tolerance meters */
	@Override
	public void setJitterToleranceMeters(int j) {
		jitter_tolerance_meters = j;
	}

	/** Set the jitter tolerance meters */
	public void doSetJitterToleranceMeters(int j) throws TMSException {
		if (j != jitter_tolerance_meters) {
			store.update(this, "jitter_tolerance_meters", j);
			setJitterToleranceMeters(j);
			notifyAttribute("jitterToleranceMeters");
		}
	}

	/** Get the jitter tolerance meters */
	@Override
	public int getJitterToleranceMeters() {
		return jitter_tolerance_meters;
	}

	/** Update one field in a storable database table
	 * (Used to avoid a bunch of boilerplate...) */
	private void storeUpdate(String field, Object value) {
		try {
			store.update(this, field, value);
		}
		catch (TMSException ex) {
			GPS_LOG.log("Error saving "+field+" to database: "+ex);
			ex.printStackTrace();
		}
	}
}
