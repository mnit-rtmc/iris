/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2024  Minnesota Department of Transportation
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

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.CapMsgType;
import us.mn.state.dot.tms.CapScope;
import us.mn.state.dot.tms.CapStatus;
import us.mn.state.dot.tms.TMSException;

/**
 * Common Alert Protocol alert.
 * 
 * These alerts are unprocessed, directly from a CAP feed, such as IPAWS.
 * The process method parses alert data to create AlertInfoImpl objects.
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

	/** Purge old records */
	static void purgeRecords(int days) throws TMSException {
		if (BaseObjectImpl.store != null && days > 0) {
			BaseObjectImpl.store.update("DELETE FROM " + TABLE +
				" WHERE receive_date < now() - '" + days +
				" days'::interval AND identifier NOT IN " +
				"(SELECT alert FROM cap.alert_info);");
		}
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
	public String getPKeyName() {
		return "identifier";
	}

	/** Get the primary key */
	@Override
	public String getPKey() {
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
		if (storeAlert() && checkStatus() && checkScope())
			processData();
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

	/** Process alert data */
	private void processData() {
		try {
			CapMsgType msg_type = CapMsgType.fromValue(
				alert.getString("msgType"));
			String references = alert.optString("references", "");
			String sent = alert.getString("sent");
			JSONArray infos = alert.getJSONArray("info");
			for (int i = 0; i < infos.length(); i++) {
				JSONObject info = infos.getJSONObject(i);
				AlertData data = new AlertData(identifier,
					msg_type, references, sent, info);
				data.process();
			}
		}
		catch (JSONException | ParseException | SonarException |
		       SQLException | TMSException e)
		{
			log("processData failed, " + e.getMessage());
		}
	}
}
