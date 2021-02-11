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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgis.MultiPolygon;
import org.postgis.Polygon;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.AlertConfig;
import us.mn.state.dot.tms.AlertConfigHelper;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.CapCertainty;
import us.mn.state.dot.tms.CapEvent;
import us.mn.state.dot.tms.CapMsgType;
import us.mn.state.dot.tms.CapResponseType;
import us.mn.state.dot.tms.CapScope;
import us.mn.state.dot.tms.CapSeverity;
import us.mn.state.dot.tms.CapStatus;
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

/**
 * Common Alert Protocol alert.
 * 
 * These alerts are unprocessed directly from a CAP feed, such as IPAWS.
 * Use the process method to create AlertInfoImpl objects.
 *
 * @author Douglas Lau
 * @author Michael Janson
 * @author Gordon Parikh
 */
public class CapAlert implements Storable {

	/** Alert debug log */
	static public final DebugLog LOG = new DebugLog("alert");

	/** Database table name */
	static private final String TABLE = "cap.alert";

	/** Get purge threshold (days) */
	static private int getPurgeDays() {
		return SystemAttrEnum.CAP_ALERT_PURGE_DAYS.getInt();
	}

	/** Purge old records */
	static public void purgeRecords() throws TMSException {
		int age = getPurgeDays();
		if (BaseObjectImpl.store != null && age > 0) {
			BaseObjectImpl.store.update("DELETE FROM " + TABLE +
				" WHERE receive_date < now() - '" + age +
				" days'::interval;");
		}
	}

	/** Date formatter for formatting/parsing CAP dates */
	static private final SimpleDateFormat CAP_DATE =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

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

	/** Build query to find DMS within a MultiPolygon */
	static private String buildDMSQuery(MultiPolygon mp, int th) {
		return "SELECT d.name " +
			"FROM iris." + DMS.SONAR_TYPE + " d " +
			"JOIN iris." + GeoLoc.SONAR_TYPE + " g " +
			"ON d.geo_loc=g.name " +
			"WHERE ST_DWithin('" + mp + "'," +
				"ST_Point(g.lon,g.lat)::geography," + th + ")";
	}

	/** Interval value of one hour (ms) */
	static private final long HOUR_MS = 60 * 60 * 1000;

	/** Data for one alert */
	static private class AlertData {
		CapEvent event;
		CapResponseType response_type;
		CapUrgency urgency;
		Date start_date;
		Date end_date;
		CapSeverity severity;
		CapCertainty certainty;
		String headline;
		String description;
		String instruction;
		String area_desc;
		MultiPolygon geo_poly;
		double centroid[];
		ArrayList<DMS> auto_dms = new ArrayList<DMS>();
		ArrayList<DMS> all_dms = new ArrayList<DMS>();
	}

	/** Log a message */
	private void log(String msg) {
		if (LOG.isOpen())
			LOG.log("alert " + identifier + ": " + msg);
	}

	/** CAP alert message identifier */
	private final String identifier;

	/** Get the alert identifier */
	public String getIdentifier() {
		return identifier;
	}

	/** CAP alert in JSON form */
	private final JSONObject alert;

	/** Receive date */
	private final Date receive_date = TimeSteward.getDateInstance();

	/** Create a new event.
	 * @param id CAP alert message identifier.
	 * @param al Alert in JSON form. */
	public CapAlert(String id, JSONObject al) {
		identifier = id;
		alert = al;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return TABLE;
	}

	/** Get the primary key name */
	@Override
	public String getKeyName() {
		return "identifier";
	}

	/** Get the primary key */
	@Override
	public String getKey() {
		return identifier;
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("identifier", identifier);
		map.put("alert", alert.toString());
		map.put("receive_date", receive_date);
		return map;
	}

	/** Check the alert for relevance to this system and process it for
	 *  posting.
	 *
	 *  Relevance is determined based on whether there is one or more
	 *  existing AlertConfig objects that match the alert values and
	 *  whether the alert area(s) encompass any DMS known to the system.
	 *
	 *  DMS selection uses PostGIS to handle the geospatial operations.
	 *  If at least one sign is selected, an ActionPlan object is created
	 *  to deploy the alert.
	 *
	 *  One AlertInfo object is created for each matching AlertConfig,
	 *  allowing different messages to be posted to different sign types.
	 */
	public void process() {
		log("processing");
		if (storeAlert() && checkStatus() && checkScope()) {
			CapMsgType msg_type = getMsgType();
			switch (msg_type) {
			case ALERT:
				createAlertInfos();
				return;
			case UPDATE:
				// FIXME: update existing alert infos
				createAlertInfos();
				return;
			case CANCEL:
			case ERROR:
				// FIXME: cancel existing alert infos
				return;
			default:
				return;
			}
		}
	}

	/** Store the alert in the database */
	private boolean storeAlert() {
		try {
			BaseObjectImpl.store.create(this);
			log("stored");
			return true;
		}
		catch (TMSException e) {
			String msg = e.getMessage();
			if (msg.contains("violates unique constraint"))
				log("already processed");
			else
				log("store failed, " + msg);
			return false;
		}
	}

	/** Check alert status */
	private boolean checkStatus() {
		try {
			CapStatus status = CapStatus.fromValue(alert.getString(
				"status"));
			log("status " + status);
			return status == CapStatus.ACTUAL;
		}
		catch (JSONException e) {
			log("get status, " + e.getMessage());
			return false;
		}
	}

	/** Check alert scope */
	private boolean checkScope() {
		try {
			CapScope scope = CapScope.fromValue(alert.getString(
				"scope"));
			log("scope " + scope);
			return scope == CapScope.PUBLIC;
		}
		catch (JSONException e) {
			log("get scope, " + e.getMessage());
			return false;
		}
	}

	/** Get the message type */
	private CapMsgType getMsgType() {
		try {
			CapMsgType msg_type = CapMsgType.fromValue(
				alert.getString("msgType"));
			log("msgType " + msg_type);
			return msg_type;
		}
		catch (JSONException e) {
			log("get msgType, " + e.getMessage());
			return CapMsgType.UNKNOWN;
		}
	}

	/** Create alert info records */
	private void createAlertInfos() {
		try {
			JSONArray infos = alert.getJSONArray("info");
			Date sent = CAP_DATE.parse(alert.getString("sent"));
			for (int i = 0; i < infos.length(); i++)
				createAlertInfo(infos.getJSONObject(i), sent);
		}
		catch (JSONException | ParseException | SonarException |
		       SQLException | TMSException e)
		{
			log("create infos failed, " + e.getMessage());
		}
	}

	/** Create an alert info object */
	private void createAlertInfo(JSONObject info, Date sent)
		throws JSONException, ParseException, SonarException,
		SQLException, TMSException
	{
		AlertData data = new AlertData();
		data.event = lookupEvent(info);
		if (data.event == null) {
			log("unknown event");
			return;
		}
		data.response_type = lookupResponseType(info);
		data.urgency = CapUrgency.fromValue(info.getString("urgency"));
		List<AlertConfig> configs = AlertConfigHelper.findMatching(
			data.event, data.response_type, data.urgency);
		if (configs.isEmpty()) {
			log("no matching configurations");
			return;
		}
		data.start_date = getStartDate(info, sent);
		data.end_date = getEndDate(info);
		data.severity = CapSeverity.fromValue(info.getString(
			"severity"));
		data.certainty = CapCertainty.fromValue(info.getString(
			"certainty"));
		data.headline = info.getString("headline");
		data.description = info.getString("description");
		data.instruction = info.getString("instruction");
		data.area_desc = getAreaDesc(info);
		data.geo_poly = createPolygons(info);
		if (data.geo_poly != null) {
			log("found polygons: " + data.geo_poly
				.getPolygons().length);
			data.centroid = findCentroid(data.geo_poly);
			log("centroid: " + data.centroid[0] + ", " +
				data.centroid[1]);
			findDMS(data);
			for (AlertConfig cfg: configs)
				createAlertInfo(data, cfg);
		}
	}

	/** Get the start date/time.  Checks onset time first, then effective
	 *  time, and finally sent time (which is required). */
	private Date getStartDate(JSONObject info, Date sent)
		throws JSONException, ParseException
	{
		if (info.has("onset"))
			return CAP_DATE.parse(info.getString("onset"));
		else if (info.has("effective"))
			return CAP_DATE.parse(info.getString("effective"));
		else
			return sent;
	}

	/** Get the event ending date */
	private Date getEndDate(JSONObject info) throws JSONException,
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

	/** Lookup event code */
	private CapEvent lookupEvent(JSONObject info) throws JSONException {
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
	private CapResponseType lookupResponseType(JSONObject info)
		throws JSONException
	{
		JSONArray rts = info.getJSONArray("responseType");
		for (int i = 0; i < rts.length(); i++) {
			CapResponseType crt = CapResponseType.fromValue(
				rts.getString(i));
			if (crt != CapResponseType.NONE)
				return crt;
		}
		return CapResponseType.NONE;
	}

	/** Get area description */
	private String getAreaDesc(JSONObject info) throws JSONException {
		JSONArray areas = info.getJSONArray("area");
		for (int i = 0; i < areas.length(); i++) {
			JSONObject area = areas.getJSONObject(i);
			return area.getString("areaDesc");
		}
		return null;
	}

	/** Create a MultiPolygon geography object from alert info */
	private MultiPolygon createPolygons(JSONObject info)
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
	private void createPolygons(JSONObject area, ArrayList<Polygon> polys)
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
	private void createPolygonsArr(JSONArray pgons,
		ArrayList<Polygon> polys) throws JSONException, SQLException
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

	/** Create Polygons from a "geocode" section */
	private void createPolygonsGeo(JSONArray geocode,
		ArrayList<Polygon> polys) throws JSONException, SQLException,
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

	/** Find the centroid of multi polygon */
	private double[] findCentroid(MultiPolygon mp) throws TMSException {
		double[] centroid = new double[2];
		BaseObjectImpl.store.query("SELECT ST_AsText(ST_Centroid('" +
			mp + "'));", new ResultFactory()
		{
			@Override public void create(ResultSet row)
				throws SQLException
			{
				findCentroid(row, centroid);
			}
		});
		return centroid;
	}

	/** Find the centroid of a multipolygon from a row with a POINT */
	private void findCentroid(ResultSet row, double[] centroid)
		throws SQLException
	{
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

	/** Find DMS within a MultiPolygon for an alert config */
	private void findDMS(AlertData data) throws TMSException {
		log("searching for DMS");
		findDMS(data.geo_poly, data.auto_dms, autoDmsMeters());
		if (data.auto_dms.size() > 0) {
			log("found " + data.auto_dms.size() + " auto signs");
			findDMS(data.geo_poly, data.all_dms,
				optionalDmsMeters());
			log("found " + data.all_dms.size() + " auto+opt signs");
		}
	}

	/** Find all DMS within given alert area threshold */
	private void findDMS(MultiPolygon mp, ArrayList<DMS> dms, int th)
		throws TMSException
	{
		BaseObjectImpl.store.query(buildDMSQuery(mp, th),
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

	/** Create alert info */
	private void createAlertInfo(AlertData data, AlertConfig cfg)
		throws SonarException, TMSException
	{
		ArrayList<DMS> all_dms = getAllSigns(cfg, data);
		if (all_dms != null) {
			ActionPlanImpl plan = createPlan(cfg, data);
			if (plan != null)
				createAlertInfo(data, all_dms, plan);
		}
	}

	/** Get a list of all signs affected */
	private ArrayList<DMS> getAllSigns(AlertConfig cfg, AlertData data) {
		ArrayList<DMS> all_dms = SignGroupHelper.getAllSigns(
			cfg.getSignGroup());
		all_dms.retainAll(data.all_dms);
		if (!all_dms.isEmpty())
			return all_dms;
		else {
			log("no signs found for " + cfg.getName());
			return null;
		}
	}

	/** Create an action plan for this alert */
	private ActionPlanImpl createPlan(AlertConfig cfg, AlertData data)
		throws SonarException, TMSException
	{
		ArrayList<DMS> act_dms = SignGroupHelper.getAllSigns(
			cfg.getSignGroup());
		act_dms.retainAll(data.auto_dms);
		String qmulti[] = null;
		QuickMessage qm = cfg.getQuickMessage();
		SignConfig sign_cfg = null;
		if (qm != null) {
			String multi = qm.getMulti();
			log("message template: " + multi);
			sign_cfg = qm.getSignConfig();
			qmulti = new String[3];
			qmulti[0] = generateMulti(multi,
				CapMultiBuilder.Period.BEFORE, data);
			qmulti[1] = generateMulti(multi,
				CapMultiBuilder.Period.DURING, data);
			qmulti[2] = generateMulti(multi,
				CapMultiBuilder.Period.AFTER, data);
		} else {
			log("invalid MULTI for " + cfg.getName());
			return null;
		}
		// Create action plan
		String pname = ActionPlanImpl.createUniqueName("ALERT_" +
			data.event.name() + "_%d");
		String dsc = "Alert: " + data.event.description;
		PlanPhase undep = PlanPhaseHelper.lookup("undeployed");
		PlanPhase phase = PlanPhaseHelper.lookup(lookupCurrentPhase(cfg,
			data));
		ActionPlanImpl plan = new ActionPlanImpl(pname, dsc, "alert",
			false, false, cfg.getAutoDeploy(), undep, phase);
		log("created plan " + pname);
		plan.notifyCreate();
		// Create active sign group
		SignGroup grp_act = createSignGroup(plan, "ACT", act_dms);
		// Create "before" actions
		int pre_hours = cfg.getPreAlertHours();
		if (pre_hours > 0) {
			long sd = data.start_date.getTime();
			Date before = new Date(sd - pre_hours * HOUR_MS);
			createTimeAction(plan, before, "alert_before");
			QuickMessage b_qm = createQuickMsg(plan, grp_act,
				sign_cfg, qmulti[0]);
			createDmsAction(plan, grp_act, "alert_before", b_qm);
		}
		// Create "during" actions
		createTimeAction(plan, data.start_date, "alert_during");
		QuickMessage d_qm = createQuickMsg(plan, grp_act, sign_cfg,
			qmulti[1]);
		createDmsAction(plan, grp_act, "alert_during", d_qm);
		// Create "after" actions
		int post_hours = cfg.getPostAlertHours();
		if (post_hours > 0) {
			createTimeAction(plan, data.end_date, "alert_after");
			QuickMessage a_qm = createQuickMsg(plan, grp_act,
				sign_cfg, qmulti[2]);
			createDmsAction(plan, grp_act, "alert_after", a_qm);
		}
		// Create final time action
		long ed = data.end_date.getTime();
		Date after = new Date(ed + post_hours * HOUR_MS);
		createTimeAction(plan, after, "undeployed");
		return plan;
	}

	/** Generate a MULTI message from an alert config */
	private String generateMulti(String multi, CapMultiBuilder.Period per,
		AlertData data)
	{
		CapMultiBuilder builder = new CapMultiBuilder(per,
			data.start_date, data.end_date);
		new MultiString(multi).parse(builder);
		MultiString ms = builder.toMultiString();
		log("" + per + ": " + ms.toString());
		return ms.toString();
	}

	/** Lookup the current plan phase name */
	private String lookupCurrentPhase(AlertConfig cfg, AlertData data) {
		if (cfg instanceof AlertConfigImpl) {
			AlertConfigImpl ac = (AlertConfigImpl) cfg;
			if (ac.getAutoDeploy()) {
				return ac.getCurrentPhase(data.start_date,
					data.end_date);
			}
		}
		return "undeployed";
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

	/** Create a sign group for an action plan */
	private SignGroup createSignGroup(ActionPlanImpl plan, String gv,
		List<DMS> dms) throws SonarException
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

	/** Create a quick message for an action plan */
	private QuickMessage createQuickMsg(ActionPlanImpl plan, SignGroup grp,
		SignConfig sign_cfg, String multi) throws SonarException
	{
		String tmpl = plan.getName() + "_%d";
		String qname = QuickMessageImpl.createUniqueName(tmpl);
		QuickMessageImpl qm = new QuickMessageImpl(qname, grp, sign_cfg,
			false, multi);
		qm.notifyCreate();
		log("created quick message " + qname);
		return qm;
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
	private void createAlertInfo(AlertData data, List<DMS> all_dms,
		ActionPlanImpl plan) throws SonarException
	{
		SignGroup sign_group = createSignGroup(plan, "ALL", all_dms);
		String aname = AlertInfoImpl.createUniqueName();
		String replaces = null; // FIXME
		int st = (plan.getActive())
		       ? AlertState.ACTIVE.ordinal()
		       : AlertState.PENDING.ordinal();
		AlertInfoImpl ai = new AlertInfoImpl(aname, identifier,
			replaces, data.start_date, data.end_date,
			data.event.name(), data.response_type.ordinal(),
			data.urgency.ordinal(), data.severity.ordinal(),
			data.certainty.ordinal(), data.headline,
			data.description, data.instruction, data.area_desc,
			data.geo_poly, data.centroid[0], data.centroid[1],
			sign_group, plan, st);
		log("created alert info " + aname);
		ai.notifyCreate();
	}
}
