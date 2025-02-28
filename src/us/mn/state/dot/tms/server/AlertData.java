/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
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
import java.util.Iterator;
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
import us.mn.state.dot.tms.AlertInfo;
import us.mn.state.dot.tms.AlertInfoHelper;
import us.mn.state.dot.tms.AlertMessage;
import us.mn.state.dot.tms.AlertMessageHelper;
import us.mn.state.dot.tms.AlertPeriod;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.CapCertainty;
import us.mn.state.dot.tms.CapEvent;
import us.mn.state.dot.tms.CapMsgType;
import us.mn.state.dot.tms.CapResponseType;
import us.mn.state.dot.tms.CapSeverity;
import us.mn.state.dot.tms.CapUrgency;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.PlanPhaseHelper;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignConfigHelper;
import us.mn.state.dot.tms.SignMsgPriority;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Alert Data processed from JSON info section.
 *
 * @author Douglas Lau
 * @author Michael Janson
 * @author Gordon Parikh
 */
public class AlertData {

	/** Interval value of one hour (ms) */
	static private final long HOUR_MS = 60 * 60 * 1000;

	/** Log a message */
	static private void log(String msg) {
		CapAlert.LOG.log(msg);
	}

	/** Lookup event code */
	static private CapEvent lookupEvent(JSONObject info)
		throws JSONException
	{
		CapEvent ev = CapEvent.fromDescription(info.getString("event"));
		if (info.has("eventCode")) {
			JSONArray codes = info.getJSONArray("eventCode");
			for (int i = 0; i < codes.length(); i++) {
				JSONObject code = codes.getJSONObject(i);
				String value = code.getString("value");
				CapEvent cev = CapEvent.fromCode(value);
				if (ev == null || ev == cev)
					return cev;
			}
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
		if (info.has("area")) {
			JSONArray areas = info.getJSONArray("area");
			for (int i = 0; i < areas.length(); i++) {
				JSONObject area = areas.getJSONObject(i);
				return area.getString("areaDesc");
			}
		}
		return null;
	}

	/** Create a MultiPolygon geography object from alert info */
	static private MultiPolygon createPolygons(JSONObject info)
		throws JSONException, SQLException, TMSException
	{
		ArrayList<Polygon> polys = new ArrayList<Polygon>();
		if (info.has("area")) {
			JSONArray areas = info.getJSONArray("area");
			for (int i = 0; i < areas.length(); i++)
				createPolygons(areas.getJSONObject(i), polys);
		}
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

	/** Create Polygons from a "geocode" section */
	static private void createPolygonsGeo(JSONArray geocode,
		List<Polygon> polys) throws JSONException, SQLException,
		TMSException
	{
		ArrayList<String> nws_zones = new ArrayList<String>();
		ArrayList<String> fips_codes = new ArrayList<String>();
		for (int i = 0; i < geocode.length(); i++) {
			JSONObject gc = geocode.getJSONObject(i);
			String geo = gc.getString("valueName");
			String code = gc.getString("value");
			if ("UGC".equals(geo) &&
			    code.length() == 6 &&
			    code.charAt(2) == 'Z')
				nws_zones.add(formatUGC(code));
			if (("FIPS".equals(geo) || "SAME".equals(geo)) &&
			    code.length() == 6)
				fips_codes.add(formatFIPS(code));
		}
		if (nws_zones.size() > 0)
			createPolygonsNwsZones(nws_zones, polys);
		else if (fips_codes.size() > 0)
			createPolygonsFipsCodes(fips_codes, polys);
		else
			log("no valid geocodes found!");
	}

	/** Table containing NWS forecast zone geometries */
	static private final String NWS_ZONE_TABLE = "cap.nws_zones";

	/** Create Polygons from a list of NWS forecast zones */
	static private void createPolygonsNwsZones(List<String> zones,
		List<Polygon> polys) throws JSONException, SQLException,
		TMSException
	{
		String codes = String.join(",", zones);
		log("got UGC codes: " + codes);
		BaseObjectImpl.store.query("SELECT geom FROM " + NWS_ZONE_TABLE
			+ " WHERE state_zone IN (" + codes + ");",
			new PolygonFactory(polys));
	}

	/** Result factory for building polygons */
	static private class PolygonFactory implements ResultFactory {
		private final List<Polygon> polys;
		private PolygonFactory(List<Polygon> p) {
			polys = p;
		}

		@Override
		public void create(ResultSet row) throws SQLException {
			MultiPolygon mp = SQLConnection.multiPolygon(
				row.getObject(1));
			if (mp != null) {
				Polygon[] pgons = mp.getPolygons();
				for (int i = 0; i < pgons.length; i++)
					polys.add(pgons[i]);
			} else
				log("invalid geom PostGIS table!");
		}
	}

	/** Table containing US County boundary geometries */
	static private final String NWS_COUNTIES_TABLE = "cap.nws_counties";

	/** Create Polygons from a list of SAME (FIPS) county codes */
	static private void createPolygonsFipsCodes(List<String> fips_codes,
		List<Polygon> polys) throws JSONException, SQLException,
		TMSException
	{
		String codes = String.join(",", fips_codes);
		log("got FIPS codes: " + codes);
		BaseObjectImpl.store.query("SELECT geom FROM " +
			NWS_COUNTIES_TABLE + " WHERE fips IN (" + codes + ");",
			new PolygonFactory(polys));
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

	/** Format a UGC code containing an NWS forecast zone ID.
	 *
	 *  UGC fields will come in as "{STATE}Z{CODE}" (e.g. "MNZ060").
	 *  We want "{STATE}{CODE}" (e.g. "MN060"), which matches the data from
	 *  NWS_ZONE_TABLE. */
	static private String formatUGC(String ugc) {
		return "'" + String.join("", ugc.split("Z")) + "'";
	}

	/** Format a SAME (FIPS) code containing a county ID */
	static private String formatFIPS(String fips) {
		while (fips.startsWith("0"))
			fips = fips.substring(1);
		return "'" + fips + "'";
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

	/** Lookup an alert period for a set of alert messages */
	static private AlertPeriod lookupPeriod(Set<AlertMessage> msgs) {
		AlertPeriod period = null;
		for (AlertMessage msg: msgs) {
			AlertPeriod ap = AlertPeriod.fromOrdinal(
				msg.getAlertPeriod());
			if (ap == null || (period != null && period != ap))
				log("invalid alert message: " + msg);
			else
				period = ap;
		}
		return period;
	}

	/** Lookup a phase name for an alert config / period */
	static private String lookupPhase(AlertConfig cfg, AlertPeriod ap) {
		if (ap == null)
			return null;
		switch (ap) {
			case BEFORE:
				return (cfg.getBeforePeriodHours() > 0)
				      ? PlanPhase.ALERT_BEFORE
				      : null;
			case DURING:
				return PlanPhase.ALERT_DURING;
			case AFTER:
				return (cfg.getAfterPeriodHours() > 0)
				      ? PlanPhase.ALERT_AFTER
				      : null;
			default:
				return null;
		}
	}

	/** Alert identifier */
	private final String identifier;

	/** CAP message type */
	private final CapMsgType msg_type;

	/** Message references */
	private final String references;

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
	public AlertData(String id, CapMsgType mt, String ref, String sent,
		JSONObject info) throws JSONException, ParseException,
		SonarException, SQLException, TMSException
	{
		identifier = id;
		msg_type = mt;
		references = ref;
		event = lookupEvent(info);
		response_type = lookupResponseType(info);
		urgency = CapUrgency.fromValue(info.getString("urgency"));
		severity = CapSeverity.fromValue(info.getString("severity"));
		certainty = CapCertainty.fromValue(info.getString("certainty"));
		start_date = getStartDate(info, sent);
		end_date = getEndDate(info);
		headline = info.optString("headline", "");
		description = info.optString("description", "");
		instruction = info.optString("instruction", "");
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

	/** Process alert data */
	public void process() throws SonarException, TMSException {
		switch (msg_type) {
		case ALERT:
		case UPDATE:
			createAlertInfos();
			return;
		case CANCEL:
		case ERROR:
			cancelAlertInfos();
			return;
		}
	}

	/** Create alert info for all matching configurations */
	private void createAlertInfos() throws SonarException, TMSException {
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
	private void findSigns(TreeSet<DMS> signs, int th) throws TMSException {
		BaseObjectImpl.store.query(buildDMSQuery(geo_poly, th),
			new ResultFactory()
		{
			@Override public void create(ResultSet row) {
				try {
					String nm = row.getString(1);
					log("found DMS, " + nm);
					DMS d = DMSHelper.lookup(nm);
					if (d != null)
						signs.add(d);
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
		String cht = cfg.getDmsHashtag();
		if (cht == null)
			return;
		TreeSet<DMS> plan_dms = new TreeSet<DMS>();
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS d = it.next();
			if (all_dms.contains(d)) {
				if (new Hashtags(d.getNotes()).contains(cht))
					plan_dms.add(d);
			}
		}
		ActionPlanImpl plan = createPlan(cfg, plan_dms);
		if (plan != null)
			createAlertInfo(plan, plan_dms);
	}

	/** Create an action plan for this alert */
	private ActionPlanImpl createPlan(AlertConfig cfg, Set<DMS> plan_dms)
		throws SonarException
	{
		Set<AlertMessage> msgs = AlertMessageHelper
			.getValidMessages(cfg);
		if (msgs.isEmpty()) {
			log("no messages for " + cfg.getName());
			return null;
		}
		// only keep signs with sign configs defined by alert messages
		plan_dms.retainAll(lookupSigns(msgs));
		if (plan_dms.isEmpty()) {
			log("no signs for " + cfg.getName());
			return null;
		}
		// Create action plan
		String pname = ActionPlanImpl.createUniqueName("ALERT_" +
			event.name() + "_%d");
		String notes = "#Alert " + event.description;
		String cur_phase = lookupCurrentPhase(cfg);
		ActionPlanImpl plan = new ActionPlanImpl(pname, notes,
			false, false, false, cfg.getAutoDeploy(),
			PlanPhase.UNDEPLOYED, cur_phase);
		log("created plan " + pname);
		plan.notifyCreate();
		createTimeActions(cfg, plan);
		int num = 1;
		for (Map.Entry<MsgPattern, TreeSet<AlertMessage>> ent:
		     patMsgs(msgs))
		{
			MsgPattern pat = ent.getKey();
			Set<AlertMessage> p_msgs = ent.getValue();
			String ph = lookupPhase(cfg, lookupPeriod(p_msgs));
			PlanPhase phase = PlanPhaseHelper.lookup(ph);
			if (phase == null) {
				if (ph != null)
					log("plan phase not found: " + ph);
				continue;
			}
			Set<DMS> signs = lookupSigns(p_msgs);
			signs.retainAll(plan_dms);
			if (signs.isEmpty())
				continue;
			signs.retainAll(auto_dms);
			String ht = createHashtags(plan, "a" + num, signs);
			createDmsAction(plan, ht, phase, pat);
			num++;
		}
		return plan;
	}

	/** Lookup signs for a set of alert messages */
	private Set<DMS> lookupSigns(Set<AlertMessage> msgs) {
		// first, find all sign configurations
		TreeSet<SignConfig> cfgs = new TreeSet<SignConfig>();
		for (AlertMessage msg: msgs) {
			SignConfig sc = msg.getSignConfig();
			if (sc != null)
				cfgs.add(sc);
		}
		// then, look up the signs with those configs
		TreeSet<DMS> signs = new TreeSet<DMS>();
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS dms = it.next();
			SignConfig sc = dms.getSignConfig();
			if (sc != null && cfgs.contains(sc))
				signs.add(dms);
		}
		return signs;
	}

	/** Lookup alert messages for each message pattern */
	private Set<Map.Entry<MsgPattern, TreeSet<AlertMessage>>> patMsgs(
		Set<AlertMessage> msgs)
	{
		TreeMap<MsgPattern, TreeSet<AlertMessage>> pat_msgs =
			new TreeMap<MsgPattern, TreeSet<AlertMessage>>();
		for (AlertMessage msg: msgs) {
			MsgPattern pat = msg.getMsgPattern();
			SignConfig sc = msg.getSignConfig();
			if (pat != null && sc != null) {
				if (!pat_msgs.containsKey(pat)) {
					pat_msgs.put(
						pat,
						new TreeSet<AlertMessage>()
					);
				}
				pat_msgs.get(pat).add(msg);
			}
		}
		return pat_msgs.entrySet();
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

	/** Create the time actions for an action plan */
	private void createTimeActions(AlertConfig cfg, ActionPlanImpl plan)
		throws SonarException
	{
		// Create "before" action
		int before_hours = cfg.getBeforePeriodHours();
		if (before_hours > 0) {
			long sd = start_date.getTime();
			Date before = new Date(sd - before_hours * HOUR_MS);
			createTimeAction(plan, before, PlanPhase.ALERT_BEFORE);
		}
		// Create "during" action
		createTimeAction(plan, start_date, PlanPhase.ALERT_DURING);
		// Create "after" action
		int after_hours = cfg.getAfterPeriodHours();
		if (after_hours > 0)
			createTimeAction(plan, end_date, PlanPhase.ALERT_AFTER);
		// Create final time action
		long ed = end_date.getTime();
		Date after = new Date(ed + after_hours * HOUR_MS);
		createTimeAction(plan, after, PlanPhase.UNDEPLOYED);
	}

	/** Create a time action for an action plan */
	private void createTimeAction(ActionPlanImpl plan, Date dt, String ph)
		throws SonarException
	{
		String pname = plan.getName();
		String tname = TimeActionImpl.createUniqueName(pname + "_%d");
		TimeActionImpl ta = new TimeActionImpl(tname, pname, null, dt,
			dt, ph);
		if (ta.getPhase() == null)
			log("plan phase not found, " + ph);
		log("created time action " + tname);
		ta.notifyCreate();
	}

	/** Create hashtags for a set of DMS in an action plan */
	private String createHashtags(ActionPlanImpl plan, String suffix,
		Set<DMS> signs) throws SonarException
	{
		// Plan names are "ALERT_" + event + "_" + num,
		// ex. ALERT_WSW_37 or ALERT_BZW_15
		String[] parts = plan.getName().split("_", 3);
		String num = parts[parts.length - 1];
		String ht = "#Alert" +
			event.name().substring(0, 1).toUpperCase() +
			event.name().substring(1).toLowerCase() + num + suffix;
		for (DMS d: signs) {
			if (d instanceof DMSImpl) {
				DMSImpl dms = (DMSImpl) d;
				dms.addHashtagNotify(ht);
			}
		}
		return ht;
	}

	/** Create a DMS action for an action plan */
	private void createDmsAction(ActionPlanImpl plan, String ht,
		PlanPhase phase, MsgPattern pat) throws SonarException
	{
		String tmpl = plan.getName() + "_%d";
		String dname = DeviceActionImpl.createUniqueName(tmpl);
		int mp = SignMsgPriority.low_4.ordinal();
		DeviceActionImpl da = new DeviceActionImpl(dname, plan, phase,
			ht, pat, mp);
		log("created DMS action " + dname);
		da.notifyCreate();
	}

	/** Create an alert info */
	private void createAlertInfo(ActionPlanImpl plan, Set<DMS> plan_dms)
		throws SonarException
	{
		String aht = createHashtags(plan, "", plan_dms);
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
			centroid[0], centroid[1], aht, plan, st);
		log("created alert info " + aname);
		ai.notifyCreate();
	}

	/** Cancel alert infos with matching identifiers */
	private void cancelAlertInfos() throws SonarException, TMSException {
		log("cancelling alert " + references);
		List<String> refs = parseRefs();
		Iterator<AlertInfo> it = AlertInfoHelper.iterator();
		while (it.hasNext()) {
			AlertInfo ai = it.next();
			if (refs.contains(ai.getAlert()) &&
			    ai instanceof AlertInfoImpl)
			{
				AlertInfoImpl aii = (AlertInfoImpl) ai;
				aii.clear();
				log("cancelling alert info " + ai.getName());
			}
		}
	}

	/** Parse reference identifiers */
	private List<String> parseRefs() {
		ArrayList<String> refs = new ArrayList<String>();
		// Refernces are separated by whitespace
		for (String ref: references.split("\\s+")) {
			// Reference format: sender,identifier,sent
			String[] vals = ref.split(",", 3);
			if (vals.length >= 2)
				refs.add(vals[1]);
		}
		return refs;
	}
}
