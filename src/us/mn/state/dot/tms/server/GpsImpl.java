/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
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
import java.lang.Math;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.Gps;
import us.mn.state.dot.tms.GpsHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.GpsPoller;

/**
 * Implements a GPS (Global Positioning System) device
 *
 * @author John L. Stanley - SRF Consulting
 */
public class GpsImpl extends DeviceImpl implements Gps {

	/** GPS debug log */
	static public final DebugLog GPS_LOG = new DebugLog("gps");

	/** Load all the GPS */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, GpsImpl.class);
		store.query("SELECT name, controller, pin, " +
			"gps_enable, device_name, device_class, " +
			"poll_datetime, sample_datetime, " +
			"sample_lat, sample_lon, " +
			"comm_status, error_status, " +
			"jitter_tolerance_meters FROM iris." + SONAR_TYPE  + ";",
			new ResultFactory()
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

	private static Long safeGetTime(Timestamp ts) {
		return (ts == null) ? 0 : ts.getTime();
	}

	public GpsImpl(String xname) throws TMSException, SonarException {
		super(xname);
		gps_enable = true;
		device_name = xname.substring(0, xname.length()-4);
		device_class = "DMS";
		pollDatetime = null;
		sampleDatetime = null;
		sample_lat = new Double(0.0);
		sample_lon = new Double(0.0);
		comm_status = null;
		error_status = null;
		jitter_tolerance_meters = 100;
	}

	/** Test for valid location */
	static private boolean isValidLocation(Double dLat, Double dLon) {
		return ((dLat != null)
		     && (dLon != null)
		     && (dLat != 0.0)
		     && (dLon != 0.0));
	}

	/** Save a location change to the _gps table.
	 * (Also updates the _gps.sample_datetime field.)
	 * @param gps GpsImpl to save to
	 * @param dNewLat Latitude
	 * @param dNewLon Longitude
	 */
	private static void changeGpsLocation(GpsImpl gps,
			Double dNewLat,	Double dNewLon) {
		if ((gps != null) && isValidLocation(dNewLat, dNewLon)) {
			try {
				gps.doSetSampleLat(dNewLat);
				gps.doSetSampleLon(dNewLon);
				gps.doSetSampleDatetime(gps.getPollDatetime());
				gps.doSetCommStatus("Done");
			} catch (TMSException ex) {
				GPS_LOG.log("Error updating gps record: "+ex);
			}
		}
	}

	/** Save a location change to the geo_loc table
	 *  and update the GIS-info fields.
	 * @param geoloc_dev GeoLocImpl to save to
	 * @param dNewLat Latitude
	 * @param dNewLon Longitude
	 */
	private static void changeDeviceLocation(GeoLocImpl geoloc_dev,
		Double dNewLat,	Double dNewLon) {
		if ((geoloc_dev != null) && isValidLocation(dNewLat, dNewLon)) {
			try {
				geoloc_dev.setLatNotify(dNewLat);
				geoloc_dev.setLonNotify(dNewLon);
				geoloc_dev.doCalculateGIS();
			} catch (TMSException ex) {
				GPS_LOG.log("Error updating geoloc record: "+ex);
			}
		}
	}

	/** Save a device's lat/long values and update the device's GIS info.
	 * This can be called from regular (TAIP, NMEA, or RedLion) GPS polling
	 * code as well as NTCIP-internal GPS polling code (when there might
	 * not be a GPS record associated with the NTCIP device).  If no gps
	 * record is associated with the device, the IRIS system parameter
	 * GPS_NTCIP_JITTER_M is used as a distance for the jitter filter.
	 * @param sDevname Device name (parent-device name, not the gps name)
	 * @param dNewLat  New latitude
	 * @param dNewLon  New longitude
	 * @param bForce   If true, overrides jitter filter.
	 */
	public static void saveDeviceLocation(String sDevname,
			double dNewLat,
			double dNewLon,
			boolean bForce) {
		Double dOldLat = 0.0, dOldLon = 0.0;
		int jitter = 0;
		GpsImpl gps = lookupGpsImplForDevice(sDevname);
		GeoLocImpl geoloc_dev =
				GeoLocImpl.lookupGeoLocImplForDevice(sDevname);
		// get old location and appropriate jitter value
		if (gps != null) {
			dOldLat = gps.getSampleLat();
			dOldLon = gps.getSampleLon();
			jitter  = gps.getJitterToleranceMeters();
		} else if (geoloc_dev != null) {
			dOldLat = geoloc_dev.getLat();
			dOldLon = geoloc_dev.getLon();
		}
		// always save the _gps location
		changeGpsLocation(gps, dNewLat, dNewLon);
		// save the parent-device location when appropriate
		if (geoloc_dev != null) {
			if (isValidLocation(dOldLat, dOldLon) && !bForce) {
				Position p0 = new Position(dOldLat, dOldLon);
				Position p1 = new Position(dNewLat, dNewLon);
				double metersMoved = p0.distanceHaversine(p1);
				if (metersMoved < jitter)
					return; // haven't moved far enough to matter...
			}
			changeDeviceLocation(geoloc_dev, dNewLat, dNewLon);
		}
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
	}

	/** Set the controller to which this GPS is assigned */
	@Override
	public void setController(Controller c) {
		super.setController(c);
	}

	/** Request a device operation (query message, test pixels, etc.) */
	public void sendDeviceRequest(DeviceRequest dr) {
		GpsPoller p = getGpsPoller();
		if (p != null)
			p.sendRequest(this, dr);
	}

	/** Request a device operation (query message, test pixels, etc.) */
	@Override
	public void setDeviceRequest(int r) {
		sendDeviceRequest(DeviceRequest.fromOrdinal(r));
	}

	/** Get the GPS poller */
	private GpsPoller getGpsPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof GpsPoller) ? (GpsPoller)dp : null;
	}

	protected void logError(String msg) {
		if (GPS_LOG.isOpen())
			GPS_LOG.log(getName() + ": " + msg);
	}

	/** Test if GPS is online (active and not failed) */
	public boolean isOnline() {
		return isActive() && !isFailed();
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
			storeUpdate("gps_enable", agps_enable);
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
	public void doSetDeviceName(String adevice_name)
			throws TMSException {
		if (adevice_name != device_name) {
			storeUpdate("device_name", adevice_name);
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
	public void doSetDeviceClass(String adevice_class)
			throws TMSException {
		if (adevice_class != device_class) {
			storeUpdate("device_class", adevice_class);
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
	@Override
	public void setPollDatetime(Long xPollDatetime) {
		pollDatetime = xPollDatetime;
	}

	/** Set the last polled date & time */
	public void doSetPollDatetime(Long xPollDatetime) {
		if ((xPollDatetime != null)
		 && !xPollDatetime.equals(pollDatetime)) {
			storeUpdate("poll_datetime",
					asTimestamp(xPollDatetime));
			pollDatetime = xPollDatetime;
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

	/** Set the last successful poll date & time */
	@Override
	public void setSampleDatetime(Long xSampleDatetime) {
		sampleDatetime = xSampleDatetime;
	}

	/** Set the last successful poll date & time */
	public void doSetSampleDatetime(Long xSampleDatetime)
			throws TMSException {
		if (!xSampleDatetime.equals(sampleDatetime)) {
			storeUpdate("sample_datetime",
					asTimestamp(xSampleDatetime));
			setSampleDatetime(xSampleDatetime);
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
	@Override
	public void setSampleLat(double asample_lat) {
		sample_lat = asample_lat;
	}

	/** Set the most recent latitude */
	public void doSetSampleLat(double asample_lat)
			throws TMSException {
		if (asample_lat != sample_lat) {
			storeUpdate("sample_lat", asample_lat);
			setSampleLat(asample_lat);
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
	@Override
	public void setSampleLon(double asample_lon) {
		sample_lon = asample_lon;
	}

	/** Set the most recent longitude */
	public void doSetSampleLon(double asample_lon)
			throws TMSException {
		if (asample_lon != sample_lon) {
			storeUpdate("sample_lon", asample_lon);
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
	@Override
	public void setCommStatus(String acomm_status) {
		comm_status = acomm_status;
	}

	/** Set the comm status */
	public void doSetCommStatus(String acomm_status) {
		if (acomm_status != comm_status) {
			storeUpdate("comm_status", acomm_status);
			setCommStatus(acomm_status);
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
	@Override
	public void setErrorStatus(String aerror_status) {
		error_status = aerror_status;
	}

	/** Set the error status */
	public void doSetErrorStatus(String aerror_status) {
		if (aerror_status != null) {
			if (aerror_status.length() > 25)
				aerror_status = aerror_status.substring(0, 25);
			if (aerror_status != error_status) {
				storeUpdate("error_status", aerror_status);
				setErrorStatus(aerror_status);
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
	public void setJitterToleranceMeters(int jitter_m) {
		jitter_tolerance_meters = jitter_m;
	}

	/** Set the jitter tolerance meters */
	public void doSetJitterToleranceMeters(int jitter_m)
		throws TMSException
	{
		if (jitter_m != jitter_tolerance_meters) {
			storeUpdate("jitter_tolerance_meters", jitter_m);
			setJitterToleranceMeters(jitter_m);
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

	/** Given a GPS name, lookup the GpsImpl
	 *
	 * @param sGpsName
	 * @return Returns the associated GpsImpl or null.
	 */
	public static GpsImpl lookupGpsImpl(String sGpsName) {
		Gps proxy = GpsHelper.lookup(sGpsName);
		if ((proxy != null) && (proxy instanceof GpsImpl))
			return (GpsImpl)proxy;
		return null;
	}

	/** Given a device name, lookup the associated GpsImpl
	 *  (if there is one)
	 *
	 * @param dev
	 * @return Returns the associated GpsImpl or null.
	 */
	public static GpsImpl lookupGpsImplForDevice(
			String sDevName) {
		return lookupGpsImpl(sDevName+"_gps");
	}

	/** Given a device, lookup the associated
	 *  GpsImpl (if there is one)
	 *
	 * @param dev
	 * @return Returns the associated GpsImpl or null.
	 */
	public static GpsImpl lookupGpsImplForDevice(
			DeviceImpl primarydev) {
		return lookupGpsImplForDevice(primarydev.getName());
	}
}
