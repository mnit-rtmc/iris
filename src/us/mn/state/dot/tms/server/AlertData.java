/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgis.MultiPolygon;
import org.postgis.Polygon;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.AlertConfig;
import us.mn.state.dot.tms.AlertConfigHelper;
import us.mn.state.dot.tms.AlertMessage;
import us.mn.state.dot.tms.AlertMessageHelper;
import us.mn.state.dot.tms.AlertPeriod;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.CapCertainty;
import us.mn.state.dot.tms.CapEvent;
import us.mn.state.dot.tms.CapResponseType;
import us.mn.state.dot.tms.CapSeverity;
import us.mn.state.dot.tms.CapUrgency;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.PlanPhaseHelper;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * Alert Data processed from JSON info section.
 *
 * @author Douglas Lau
 * @author Michael Janson
 * @author Gordon Parikh
 */
public class AlertData {

	/** Log a message */
	static private void log(String msg) {
		CapAlert.LOG.log(msg);
	}

	/** Lookup event code */
	static private CapEvent lookupEvent(JSONObject info)
		throws JSONException
	{
		CapEvent ev = CapEvent.fromDescription(info.getString("event"));
		JSONArray codes = info.getJSONArray("eventCode");
		for (int i = 0; i < codes.length(); i++) {
			JSONObject code = codes.getJSONObject(i);
			String value = code.getString("value");
			CapEvent cev = CapEvent.fromCode(value);
			if (ev == null || ev == cev)
				return cev;
		}
		return ev;
	}

	/** Lookup a CAP response type (only first) */
	static private CapResponseType lookupResponseType(JSONObject info)
		throws JSONException
	{
		if (info.has("responseType")) {
			JSONArray rts = info.getJSONArray("responseType");
			for (int i = 0; i < rts.length(); i++) {
				CapResponseType crt = CapResponseType.fromValue(
					rts.getString(i));
				if (crt != CapResponseType.NONE)
					return crt;
			}
		}
		return CapResponseType.NONE;
	}

	/** Date formatter for formatting/parsing CAP dates */
	static private final SimpleDateFormat CAP_DATE =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

	/** Get the start date/time.  Checks onset time first, then effective
	 *  time, and finally sent time (which is required). */
	static private Date getStartDate(JSONObject info, String sent)
		throws JSONException, ParseException
	{
		if (info.has("onset"))
			return CAP_DATE.parse(info.getString("onset"));
		else if (info.has("effective"))
			return CAP_DATE.parse(info.getString("effective"));
		else
			return CAP_DATE.parse(sent);
	}

	/** Get the event ending date */
	static private Date getEndDate(JSONObject info) throws JSONException,
		ParseException
	{
		JSONArray params = info.getJSONArray("parameter");
		for (int i = 0; i < params.length(); i++) {
			JSONObject param = params.getJSONObject(i);
			if (param.getString("valueName").equals(
				"eventEndingTime"))
			{
				String value = param.getString("value");
				return CAP_DATE.parse(value);
			}
		}
		// No eventEndingTime parameter found; use expires instead
		return CAP_DATE.parse(info.getString("expires"));
	}

	/** Get area description */
	static private String getAreaDesc(JSONObject info)
		throws JSONException
	{
		JSONArray areas = info.getJSONArray("area");
		for (int i = 0; i < areas.length(); i++) {
			JSONObject area = areas.getJSONObject(i);
			return area.getString("areaDesc");
		}
		return null;
	}

	/** Create a MultiPolygon geography object from alert info */
	static private MultiPolygon createPolygons(JSONObject info)
		throws JSONException, SQLException, TMSException
	{
		ArrayList<Polygon> polys = new ArrayList<Polygon>();
		JSONArray areas = info.getJSONArray("area");
		for (int i = 0; i < areas.length(); i++)
			createPolygons(areas.getJSONObject(i), polys);
		return polys.size() > 0
		      ? new MultiPolygon(polys.toArray(new Polygon[0]))
		      : null;
	}

	/** Create Polygons from an area segment.
	 *
	 *  If a polygon section is found, it is used to add polygons to the
	 *  list.  Otherwise, the geocode section is checked for UGC codes. */
	static private void createPolygons(JSONObject area, List<Polygon> polys)
		throws JSONException, SQLException, TMSException
	{
		if (area.has("polygon"))
			createPolygonsArr(area.getJSONArray("polygon"), polys);
		else if (area.has("geocode"))
			createPolygonsGeo(area.getJSONArray("geocode"), polys);
		else
			log("no polygon or geocode section in area!");
	}

	/** Create Polygons from a JSON array */
	static private void createPolygonsArr(JSONArray pgons,
		List<Polygon> polys) throws JSONException, SQLException
	{
		for (int i = 0; i < pgons.length(); i++) {
			String ps = pgons.getString(i);
			log("got polygon: " + ps);
			Polygon pg = parseCapPolygon(ps);
			if (pg != null)
				polys.add(pg);
			else
				log("invalid polygon!");
		}
	}

	/** Parse a CAP alert polygon section and format as WKT syntax used by
	 *  PostGIS.
	 *
	 *  The string comes in as space-delimited coordinate pairs
	 *  (which themselves are separated by commas) in lat, lon order,
	 *  e.g.: 45.0,-93.0 45.0,-93.1 ...
	 *  We need something that looks like this (note coordinates are in
	 *  lon, lat order (which is x, y)
	 *  POLYGON((-93.0 45.0, -93.1 45.0, ...))
	 */
	static private Polygon parseCapPolygon(String cap) throws SQLException {
		ArrayList<String> lon_lat = new ArrayList<String>();
		for (String c: cap.split(" ")) {
			// swap "lat,lon" with "lon lat"
			String latlon[] = c.split(",");
			if (latlon.length == 2)
				lon_lat.add(latlon[1] + ' ' + latlon[0]);
			else
				return null;
		}
		String pgon = "POLYGON((" + String.join(",", lon_lat) + "))";
		return new Polygon(pgon);
	}

	/** Table containing NWS Forecast Zone Geometries.  This can be obtained
	 *  / updated from the NWS by going to this website and importing the
	 *  shapefile into PostGIS: https://www.weather.gov/gis/PublicZones. */
	static private final String NWS_ZONE_TABLE = "cap.nws_zones";

	/** Create Polygons from a "geocode" section */
	static private void createPolygonsGeo(JSONArray geocode,
		List<Polygon> polys) throws JSONException, SQLException,
		TMSException
	{
		ArrayList<String> zones = new ArrayList<String>();
		for (int i = 0; i < geocode.length(); i++) {
			JSONObject gc = geocode.getJSONObject(i);
			if ("UGC".equals(gc.getString("valueName")))
				zones.add(formatUGC(gc.getString("value")));
			// FIXME: if valueName is "SAME", use FIPS code
		}
		if (zones.isEmpty()) {
			log("no UGC codes found!");
			return;
		}
		String codes = String.join(",", zones);
		log("got UGC codes: " + codes);
		BaseObjectImpl.store.query("SELECT geom FROM " + NWS_ZONE_TABLE
			+ " WHERE state_zone IN (" + codes + ");",
			new ResultFactory()
		{
			@Override
			public void create(ResultSet row) throws SQLException {
				MultiPolygon mp = SQLConnection.multiPolygon(
					row.getObject(1));
				if (mp != null) {
					Polygon[] pgons = mp.getPolygons();
					for (int i = 0; i < pgons.length; i++)
						polys.add(pgons[i]);
				} else
					log("invalid geom in zone table!");
			}
		});
	}

	/** Build query to find DMS within a MultiPolygon */
	static private String buildDMSQuery(MultiPolygon mp, int th) {
		return "SELECT d.name " +
			"FROM iris." + DMS.SONAR_TYPE + " d " +
			"JOIN iris." + GeoLoc.SONAR_TYPE + " g " +
			"ON d.geo_loc=g.name " +
			"WHERE ST_DWithin('" + mp + "'," +
				"ST_Point(g.lon,g.lat)::geography," + th + ")";
	}

	/** Format a UGC code.
	 *
	 *  UGC fields will come in as "{STATE}Z{CODE}" (e.g. "MNZ060").
	 *  We want "{STATE}{CODE}" (e.g. "MN060"), which matches the data from
	 *  NWS_ZONE_TABLE. */
	static private String formatUGC(String ugc) {
		return "'" + String.join("", ugc.split("Z")) + "'";
	}

	/** Get the distance threshold for auto DMS */
	static private int autoDmsMeters() {
		return SystemAttrEnum.ALERT_SIGN_THRESH_AUTO_METERS.getInt();
	}

	/** Get the distance threshold for auto plus optional DMS */
	static private int optionalDmsMeters() {
		return SystemAttrEnum.ALERT_SIGN_THRESH_AUTO_METERS.getInt() +
		       SystemAttrEnum.ALERT_SIGN_THRESH_OPT_METERS.getInt();
	}

	/** Alert identifier */
	private final String identifier;

	/** CAP event */
	private final CapEvent event;

	/** CAP response type */
	private final CapResponseType response_type;

	/** CAP urgency */
	private final CapUrgency urgency;

	/** CAP severity */
	private final CapSeverity severity;

	/** CAP certainty */
	private final CapCertainty certainty;

	/** Alert start date */
	private final Date start_date;

	/** Alert end date */
	private final Date end_date;

	/** Headline text */
	private final String headline;

	/** Alert description text */
	private final String description;

	/** Response instruction */
	private final String instruction;

	/** Description of affected area */
	private final String area_desc;

	/** Area polygons */
	private final MultiPolygon geo_poly;

	/** Centroid of area */
	private final double[] centroid = new double[2];

	/** Automatically selected signs */
	private final TreeSet<DMS> auto_dms = new TreeSet<DMS>();

	/** Automatic plus optional signs */
	private final TreeSet<DMS> all_dms = new TreeSet<DMS>();

	/** Create alert data from JSON info */
	public AlertData(String id, JSONObject info, String sent)
		throws JSONException, ParseException, SonarException,
		SQLException, TMSException
	{
		identifier = id;
		event = lookupEvent(info);
		response_type = lookupResponseType(info);
		urgency = CapUrgency.fromValue(info.getString("urgency"));
		severity = CapSeverity.fromValue(info.getString("severity"));
		certainty = CapCertainty.fromValue(info.getString("certainty"));
		start_date = getStartDate(info, sent);
		end_date = getEndDate(info);
		headline = info.getString("headline");
		description = info.getString("description");
		instruction = info.getString("instruction");
		area_desc = getAreaDesc(info);
		geo_poly = createPolygons(info);
		if (geo_poly != null) {
			log("found polygons: " + geo_poly.getPolygons().length);
			findCentroid();
			log("centroid: " + centroid[0] + ", " + centroid[1]);
		}
	}

	/** Find the centroid of multi polygon */
	private void findCentroid() throws TMSException {
		BaseObjectImpl.store.query("SELECT ST_AsText(ST_Centroid('" +
			geo_poly + "'));", new ResultFactory()
		{
			@Override public void create(ResultSet row)
				throws SQLException
			{
				findCentroid(row);
			}
		});
	}

	/** Find the centroid of a multipolygon from a row with a POINT */
	private void findCentroid(ResultSet row) throws SQLException {
		// parse out the lat/long from the POINT(lon lat) string
		String point = row.getString(1);
		String lonlat = point.replace("POINT(", "").replace(")", "");
		String[] ll = lonlat.split(" ");
		if (ll.length == 2) {
			centroid[1] = Double.parseDouble(ll[0]); // lon
			centroid[0] = Double.parseDouble(ll[1]); // lat
		} else
			log("invalid point: " + lonlat);
	}

	/** Create alert info for all matching configurations */
	public void createAlertInfos() throws SonarException, TMSException {
		List<AlertConfig> configs = AlertConfigHelper.findMatching(
			event, response_type, urgency, severity, certainty);
		if (!configs.isEmpty()) {
			if (findSigns()) {
				for (AlertConfig cfg: configs)
					createAlertInfo(cfg);
			}
		} else
			log("no matching configurations");
	}

	/** Find signs within the alert area */
	private boolean findSigns() throws TMSException {
		log("searching for DMS");
		findSigns(all_dms, optionalDmsMeters());
		if (all_dms.size() > 0) {
			log("found " + all_dms.size() + " auto+opt signs");
			findSigns(auto_dms, autoDmsMeters());
			log("found " + auto_dms.size() + " auto signs");
			return true;
		} else {
			log("no signs found");
			return false;
		}
	}

	/** Find all signs within given alert area threshold */
	private void findSigns(TreeSet<DMS> dms, int th) throws TMSException {
		BaseObjectImpl.store.query(buildDMSQuery(geo_poly, th),
			new ResultFactory()
		{
			@Override public void create(ResultSet row) {
				try {
					String dnm = row.getString(1);
					log("found DMS, " + dnm);
					DMS d = DMSHelper.lookup(dnm);
					if (d != null)
						dms.add(d);
				}
				catch (SQLException e) {
					log("finding DMS, " + e.getMessage());
				}
			}
		});
	}

	/** Create alert info for one configuration */
	private void createAlertInfo(AlertConfig cfg) throws SonarException,
		TMSException
	{
		Set<DMS> signs = AlertConfigHelper.getAllSigns(cfg);
		signs.retainAll(all_dms);
		if (!signs.isEmpty()) {
			ActionPlanImpl plan = createPlan(cfg);
			if (plan != null)
				createAlertInfo(plan, signs);
		} else
			log("no signs found for " + cfg.getName());
	}

	/** Create an action plan for this alert */
	private ActionPlanImpl createPlan(AlertConfig cfg) throws SonarException
	{
		Set<AlertMessage> msgs = AlertMessageHelper
			.getValidMessages(cfg);
		if (msgs.isEmpty()) {
			log("no messages for " + cfg.getName());
			return null;
		}
		// Create action plan
		String pname = ActionPlanImpl.createUniqueName("ALERT_" +
			event.name() + "_%d");
		String dsc = "Alert: " + event.description;
		PlanPhase undep = PlanPhaseHelper.lookup(PlanPhase.UNDEPLOYED);
		PlanPhase phase = PlanPhaseHelper.lookup(lookupCurrentPhase(
			cfg));
		ActionPlanImpl plan = new ActionPlanImpl(pname, dsc, "alert",
			false, false, cfg.getAutoDeploy(), undep, phase);
		log("created plan " + pname);
		plan.notifyCreate();
		createTimeActions(cfg, plan);
		Map<SignGroup, SignGroup> act_groups = createActiveGroups(plan,
			msgs);
		for (AlertMessage msg: msgs) {
			SignGroup sg = msg.getSignGroup();
			SignGroup asg = act_groups.get(sg);
			if (asg != null)
				createDmsActions(cfg, plan, msg, asg);
		}
		return plan;
	}

	/** Lookup the current plan phase name */
	private String lookupCurrentPhase(AlertConfig cfg) {
		if (cfg instanceof AlertConfigImpl) {
			AlertConfigImpl ac = (AlertConfigImpl) cfg;
			if (ac.getAutoDeploy())
				return ac.getCurrentPhase(start_date, end_date);
		}
		return PlanPhase.UNDEPLOYED;
	}

	/** Interval value of one hour (ms) */
	static private final long HOUR_MS = 60 * 60 * 1000;

	/** Create the time actions for an action plan */
	private void createTimeActions(AlertConfig cfg, ActionPlanImpl plan)
		throws SonarException
	{
		// Create "before" action
		int before_hours = cfg.getBeforePeriodHours();
		if (before_hours > 0) {
			long sd = start_date.getTime();
			Date before = new Date(sd - before_hours * HOUR_MS);
			createTimeAction(plan, before, "alert_before");
		}
		// Create "during" action
		createTimeAction(plan, start_date, "alert_during");
		// Create "after" action
		int after_hours = cfg.getAfterPeriodHours();
		if (after_hours > 0)
			createTimeAction(plan, end_date, "alert_after");
		// Create final time action
		long ed = end_date.getTime();
		Date after = new Date(ed + after_hours * HOUR_MS);
		createTimeAction(plan, after, PlanPhase.UNDEPLOYED);
	}

	/** Create a time action for an action plan */
	private void createTimeAction(ActionPlanImpl plan, Date dt, String ph)
		throws SonarException
	{
		String tname = TimeActionImpl.createUniqueName(plan.getName() +
			"_%d");
		PlanPhase phase = PlanPhaseHelper.lookup(ph);
		if (phase == null)
			log("plan phase not found, " + ph);
		TimeActionImpl ta = new TimeActionImpl(tname, plan, null, dt,
			dt, phase);
		log("created time action " + tname);
		ta.notifyCreate();
	}

	/** Create sign groups for active signs */
	private Map<SignGroup, SignGroup> createActiveGroups(
		ActionPlanImpl plan, Set<AlertMessage> msgs)
		throws SonarException
	{
		TreeMap<SignGroup, SignGroup> act_groups =
			new TreeMap<SignGroup, SignGroup>(
			new NumericAlphaComparator<SignGroup>());
		for (AlertMessage msg: msgs) {
			SignGroup sg = msg.getSignGroup();
			if (!act_groups.containsKey(sg)) {
				SignGroup asg = makeActiveGroup(plan, sg);
				act_groups.put(sg, asg);
			}
		}
		return act_groups;
	}

	/** Make "active" sign group */
	private SignGroup makeActiveGroup(ActionPlanImpl plan, SignGroup sg)
		throws SonarException
	{
		Set<DMS> signs = SignGroupHelper.getAllSigns(sg);
		signs.retainAll(auto_dms);
		return createSignGroup(plan, "ACT", signs);
	}

	/** Create a sign group for an action plan */
	private SignGroup createSignGroup(ActionPlanImpl plan, String gv,
		Set<DMS> dms) throws SonarException
	{
		String tmpl = plan.getName() + "_" + gv + "_%d";
		String gname = SignGroupImpl.createUniqueName(tmpl);
		SignGroupImpl sg = new SignGroupImpl(gname);
		sg.notifyCreate();
		for (DMS d: dms) {
			String nm = DmsSignGroupHelper.createUniqueName(tmpl);
			DmsSignGroupImpl dsg = new DmsSignGroupImpl(nm, d, sg);
			dsg.notifyCreate();
		}
		log("created sign group " + gname);
		return sg;
	}

	/** Create DMS actions */
	private void createDmsActions(AlertConfig cfg, ActionPlanImpl plan,
		AlertMessage msg, SignGroup grp) throws SonarException
	{
		AlertPeriod ap = AlertPeriod.fromOrdinal(msg.getAlertPeriod());
		QuickMessage qm = msg.getQuickMessage();
		if (ap == null || qm == null) {
			log("invalid alert message: " + msg);
			return;
		}
		switch (ap) {
		case BEFORE:
			if (cfg.getBeforePeriodHours() > 0)
				createDmsAction(plan, grp, "alert_before", qm);
			break;
		case DURING:
			createDmsAction(plan, grp, "alert_during", qm);
			break;
		case AFTER:
			if (cfg.getAfterPeriodHours() > 0)
				createDmsAction(plan, grp, "alert_after", qm);
			break;
		}
	}

	/** Create a DMS action for an action plan */
	private void createDmsAction(ActionPlanImpl plan, SignGroup sg,
		String ph, QuickMessage qm) throws SonarException
	{
		String tmpl = plan.getName() + "_%d";
		String dname = DmsActionImpl.createUniqueName(tmpl);
		PlanPhase phase = PlanPhaseHelper.lookup(ph);
		if (phase == null)
			log("plan phase not found, " + ph);
		int priority = DmsMsgPriority.ALERT_LOW.ordinal();
		DmsActionImpl da = new DmsActionImpl(dname, plan, sg, phase,
			qm, false, priority);
		log("created DMS action " + dname);
		da.notifyCreate();
	}

	/** Create an alert info */
	private void createAlertInfo(ActionPlanImpl plan, Set<DMS> signs)
		throws SonarException
	{
		SignGroup sign_group = createSignGroup(plan, "ALL", signs);
		String aname = AlertInfoImpl.createUniqueName();
		String replaces = null; // FIXME
		int st = (plan.getActive())
		       ? AlertState.ACTIVE.ordinal()
		       : AlertState.PENDING.ordinal();
		AlertInfoImpl ai = new AlertInfoImpl(aname, identifier,
			replaces, start_date, end_date, event.name(),
			response_type.ordinal(), urgency.ordinal(),
			severity.ordinal(), certainty.ordinal(), headline,
			description, instruction, area_desc, geo_poly,
			centroid[0], centroid[1], sign_group, plan, st);
		log("created alert info " + aname);
		ai.notifyCreate();
	}
}
