/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgis.MultiPolygon;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.IpawsAlert;
import us.mn.state.dot.tms.IpawsAlertHelper;
import us.mn.state.dot.tms.IpawsConfig;
import us.mn.state.dot.tms.IpawsConfigHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Job to process IPAWS alerts.  Alerts are written to the database by the
 * ipaws CommLink, which handles polling the IPAWS-OPEN server, parsing CAP
 * XMLs, and storing all alerts in the database.
 *
 * This job processes these alerts, performing filtering based on the contents
 * of the alert (including field values and geographic reach).  Irrelevant
 * alerts are marked for purging to be (optionally) deleted by a flush job
 * (partially implemented).
 *
 * This job also standardizes geographic data from the alerts and handles DMS
 * selection, message creation, client notification, and in some modes posting
 * alert messages to DMS.
 *
 * @author Gordon Parikh
 */
public class IpawsProcJob extends Job {

	/** IPAWS Debug Log */
	static private final DebugLog IPAWS_LOG = new DebugLog("ipaws");

	/** Table containing NWS Forecast Zone Geometries.  This can be obtained
	 *  / updated from the NWS by going to this website and importing the
	 *  shapefile into PostGIS: https://www.weather.gov/gis/PublicZones.
	 */
	static private final String GEOMETRY_TABLE = "iris.nws_zones";

	/** Seconds to offset this job from the start of interval.
	 *  Alerts will generally be polled at the top of each minute, so we will
	 *  run this job 30 seconds after.
	 */
	static private final int OFFSET_SECS = 30;

	/** Log an IPAWS message */
	static public void log(String msg) {
		if (IPAWS_LOG.isOpen())
			IPAWS_LOG.log(msg);
	}

	/** Check an alert deployer */
	static private boolean checkAlertDeployer(IpawsDeployerImpl dp,
		String alertId)
	{
		return alertId.equals(dp.getAlertId()) &&
		       AlertState.DEPLOYED.ordinal() == dp.getAlertState();
	}

	/** Create a new job to process IPAWS alerts in the database. */
	public IpawsProcJob() {
		super(Calendar.MINUTE, 1, Calendar.SECOND, OFFSET_SECS);
	}

	/** Process IPAWS alerts in the database. */
	@Override
	public void perform() throws Exception {
		try {
			processAllDeployers();
			processAllAlerts();
		} catch (Exception e) {
			// if we hit any exceptions, send an email alert
			e.printStackTrace();
			sendEmailAlert("Error encountered in IPAWS alert processing " +
				"system. Check the server logs for details.");
		}
	}

	/** Process all alert deployers */
	private void processAllDeployers() throws TMSException {
		Iterator<IpawsDeployerImpl> it = IpawsDeployerImpl.iterator();
		while (it.hasNext()) {
			IpawsDeployerImpl dp = it.next();
			dp.checkStateChange();
		}
	}

	/** Process all alerts */
	private void processAllAlerts() throws SQLException, TMSException,
		NoSuchFieldException
	{
		Iterator<IpawsAlertImpl> it = IpawsAlertImpl.iterator();
		while (it.hasNext()) {
			checkAlert(it.next());
		}
	}

	/** Check an IPAWS alert for action needed */
	private void checkAlert(IpawsAlertImpl ia) throws SQLException,
		TMSException, NoSuchFieldException
	{
		if (ia.getPurgeable() == null) {
			log("Processing IPAWS alert: " + ia.getName());

			String area = ia.getArea();
			if (area != null)
				log(area);

			// normalize the geometry and get a geographic object
			// (sets the polygon on the alert object for us, or
			// marks purgeable if we have to stop
			getGeogPoly(ia);

			// if we couldn't get any geometry, we have to stop
			if (ia.getGeoPoly() == null)
				return;

			// update the alert area's centroid
			updateCentroid(ia);

			// find DMS in the polygon and generate an alert
			// deployer object this will complete all processing of
			// this alert for this cycle
			ia.processAlert();
		} else if (Boolean.FALSE.equals(ia.getPurgeable())) {
			// alert has already been processed - check if it has
			// changed phases and if re-check the alert
			Date start = ia.getAlertStart();
			Date end = ia.getExpirationDate();
			Date proc = ia.getLastProcessed();
			Date now = new Date();
			if (proc == null) {
				// should never get here, but to guard against
				// nulls
				ia.setPurgeableNotify(null);
			} else if (proc.before(start) && (now.after(start) ||
				now.equals(start)) && now.before(end))
			{
				// alert hasn't been processed since before
				// alert start time and alert is now active
				// - reprocess to create a deployer with the
				// appropriate message
				log("Alert " + ia.getName() + " is starting");
				ia.processAlert();
			} else if (proc.before(end) && (now.after(end)
				|| now.equals(end)))
			{
				// deployer was for during alert - reprocess to
				// create a new deployer for post-alert (which
				// may blank the signs)
				log("Alert " + ia.getName() + " is ending");
				ia.processAlert();
			} else {
				tryPostAlert(ia);
			}
		}
	}

	/** If the phase isn't changing, get active/approved alert deployers and
	 *  try to (re)post messages */
	private void tryPostAlert(IpawsAlertImpl ia) throws TMSException {
		String alertId = ia.getIdentifier();
		Iterator<IpawsDeployerImpl> it = IpawsDeployerImpl.iterator();
		while (it.hasNext()) {
			IpawsDeployerImpl dp = it.next();
			if (checkAlertDeployer(dp, alertId))
				dp.checkStateChange();
		}
	}

	/** Use the area section of an IPAWS alert to creating a PostGIS
	 *  MultiPolygon geography object.  If a polygon section is found, it is
	 *  used to create a MultiPolygon object (one for each polygon).  If
	 *  there is no polygon, the other location information is used to look
	 *  up one or more polygons.
	 */
	private void getGeogPoly(IpawsAlertImpl ia) throws TMSException,
		SQLException, NoSuchFieldException
	{
		// get a JSON object from the area string (which is in JSON
		// syntax)
		JSONObject jo = null;
		String ps = null;
		String area = ia.getArea();
		if (area != null) {
			try {
				log(area);
				jo = new JSONObject(area);
				// get the "polygon" section
				if (jo.has("polygon"))
					ps = jo.getString("polygon");
			} catch (JSONException e) {
				e.printStackTrace();
				ia.setPurgeableNotify(true);
				return;
			}
		} else {
			ia.setPurgeableNotify(true);
			return;
		}

		// if we didn't get a polygon, check the other fields in the
		// area to find one we can use to lookup a geographical area
		if (ps == null) {
			log("No polygon, trying UGC codes...");

			// look for geocode fields in the area section
			JSONObject gj;
			try {
				gj = jo.getJSONObject("geocode");
			} catch (JSONException e) {
				// no geocode field or not a JSONObject, not
				// much we can do
				e.printStackTrace();
				ia.setPurgeableNotify(true);
				return;
			}

			// look for UGC fields
			JSONArray ugcj;
			try {
				ugcj = gj.getJSONArray("UGC");
			} catch (JSONException e) {
				// no UGC fields or not a JSONArray, not much we
				// can do
				e.printStackTrace();
				ia.setPurgeableNotify(true);
				return;
			}

			// UGC fields will come in as "<STATE>Z<CODE>", e.g.
			// "MNZ060"
			// we want "<STATE><CODE>" (e.g. "MN060") which matches
			// the data from the NWS
			// we also want a string for an array in an SQL statement
			StringBuilder sb = new StringBuilder();
			sb.append("('");
			for (int i = 0; i < ugcj.length(); ++i) {
				String iugc = ugcj.getString(i);
				String state = iugc.substring(0, 2);
				String code = iugc.substring(3);
				sb.append(state + code);
				sb.append("','");
			}
			// remove trailing comma/quote and add closing
			// parenthesis
			if (",'".equals(sb.substring(sb.length() - 2)))
				sb.setLength(sb.length() - 2);
			sb.append(")");
			String arrStr = sb.toString();
			log("Got codes: " + arrStr);

			// query the database for the result
			IpawsAlertImpl.store.query(
			"SELECT ST_AsText(ST_Multi(ST_Union(geom))) FROM " +
			GEOMETRY_TABLE + " WHERE state_zone IN " + arrStr + ";",
			new ResultFactory() {
				@Override
				public void create(ResultSet row) throws Exception {
					// we should get back a MultiPolygon string
					String polystr = row.getString(1);
					if (polystr != null && !polystr.isEmpty()) {
						MultiPolygon mp = new MultiPolygon(polystr);
						ia.setGeoPolyNotify(mp);
					} else
						log("No polygon found for UGC codes!");
				}
			});
		} else {
			// reformat the string so PostGIS will accept it
			// create and set the MultiPolygon on the alert object
			log("Got polygon: " + ps);
			String pgps = formatMultiPolyStr(ps);
			MultiPolygon mp = new MultiPolygon(pgps);
			ia.setGeoPolyNotify(mp);
		}
		if (ia.getGeoPoly() == null) {
			log("No polygon found, marking alert " +
				ia.getName() + " as purgeable");
			ia.setPurgeableNotify(true);
		}
	}

	/** Update the centroid of an alert area */
	private void updateCentroid(IpawsAlertImpl ia) throws TMSException {
		IpawsAlertImpl.store.query("SELECT ST_AsText(ST_Centroid(" +
			"geo_poly)) FROM event." + IpawsAlert.SONAR_TYPE +
			" WHERE name='" + ia.getName() + "';",
			new ResultFactory()
		{
				@Override public void create(ResultSet row)
					throws Exception
				{
					updateCentroid(ia, row);
				}
			}
		);
	}

	/** Update the centroid of an IPAWS alert from a row with a POINT */
	private void updateCentroid(IpawsAlertImpl ia, ResultSet row)
		throws Exception
	{
		// parse out the lat/long from the POINT(lon lat) string
		String pStr = row.getString(1);
		String llStr = pStr.replace("POINT(", "").replace(")", "");
		String[] ll = llStr.split(" ");
		if (ll.length == 2) {
			Double lon = Double.valueOf(ll[0]);
			Double lat = Double.valueOf(ll[1]);
			ia.setLatNotify(lat);
			ia.setLonNotify(lon);
		}
	}

	/** Reformat the text taken from the polygon section of a CAP alert's
	 *  area section to match the WKT syntax used by PostGIS.
	 *
	 *  TODO this would need some changes to work with multiple <polygon>
	 *  blocks, but NWS doesn't seem to use those.
	 */
	private String formatMultiPolyStr(String capPolyStr)
		throws NoSuchFieldException
	{
		// the string comes in as space-delimited coordinate pairs (which
		// themselves are separated by commas) in lat, lon order,
		// e.g.: 45.0,-93.0 45.0,-93.1 ...
		// we need something that looks like this (note coordinates are in
		// lon, lat order (which is x, y)
		// MULTIPOLYGON(((-93.0 45.0, -93.1 45.0, ...), (...)))

		// start a StringBuilder
		StringBuilder sb = new StringBuilder();

		// TODO this would need to change to handle multiple <polygon> blocks
		sb.append("MULTIPOLYGON(((");

		// split the polygon string on spaces to get coordinate pairs
		String coords[] = capPolyStr.split(" ");
		for (String c: coords) {
			String clatlon[] = c.split(",");
			String lat, lon;
			if (clatlon.length == 2) {
				lat = clatlon[0];
				lon = clatlon[1];
			} else {
				throw new NoSuchFieldException(
						"Problem decoding polygon field");
			}
			// add the coordinates to the string as "lon lat"
			sb.append(lon);
			sb.append(" ");
			sb.append(lat);
			sb.append(", ");
		}

		// remove the trailing comma
		if (sb.substring(sb.length()-2).equals(", "))
			sb.setLength(sb.length()-2);

		// TODO change this when fixing for multiple polygons
		sb.append(")))");

		return sb.toString();
	}

	/** Send an email alert. Generally called when an exception is hit. */
	static public void sendEmailAlert(String msg) {
		String recip =
			SystemAttrEnum.EMAIL_RECIPIENT_AWS.getString();
		EmailHandler.sendEmail("Error in IRIS IPAWS Processing System",
				msg, recip);
	}
}
