/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Parser for National Weather Service GeoJSON + CAP hybrid.
 *
 * URL: `https://api.weather.gov/alerts/active?area=XX`
 *
 * @author Douglas Lau
 */
public class NwsParser implements AlertParser {

	/** Parse alerts */
	@Override
	public List<JSONObject> parse(String doc) throws IOException {
		ArrayList<JSONObject> alerts = new ArrayList<JSONObject>();
		try {
			JSONObject jo = new JSONObject(doc);
			JSONArray features = jo.getJSONArray("features");
			for (int i = 0; i < features.length(); i++) {
				JSONObject alert = makeAlert(
					features.getJSONObject(i));
				if (alert != null)
					alerts.add(alert);
			}
		}
		catch (JSONException e) {
			throw new ParsingException(e);
		}
		return alerts;
	}

	/** Convert an NWS GeoJSON feature into a CAP alert */
	private JSONObject makeAlert(JSONObject feat) {
		JSONObject alert = new JSONObject();
		alert.putOnce("identifier", feat.getString("id"));
		JSONObject props = feat.getJSONObject("properties");
		alert.putOnce("sender", props.getString("sender"));
		alert.putOnce("sent", props.getString("sent"));
		alert.putOnce("status", props.getString("status"));
		alert.putOnce("msgType", props.getString("messageType"));
		alert.putOnce("scope", props.optString("scope", "Public"));
		JSONArray refs = props.optJSONArray("references");
		if (refs != null)
			alert.putOnce("references", makeReferences(refs));
		JSONObject info = new JSONObject();
		info.append("category", props.getString("category"));
		info.putOnce("event", props.getString("event"));
		info.append("responseType", props.getString("response"));
		info.putOnce("urgency", props.getString("urgency"));
		info.putOnce("severity", props.getString("severity"));
		info.putOnce("certainty", props.getString("centainty"));
		info.putOnce("effective", props.optString("effective"));
		info.putOnce("onset", props.optString("onset"));
		info.putOnce("expires", props.optString("expires"));
		info.putOnce("senderName", props.optString("senderName"));
		info.putOnce("headline", props.optString("headline"));
		info.putOnce("description", props.optString("description"));
		info.putOnce("instruction", props.optString("instruction"));
		makeParameters(props.optJSONObject("parameters"), info);
		info.append("area", makeArea(props));
		alert.append("info", info);
		return alert;
	}

	/** Make CAP references in `sender,identifier,sent` format */
	private String makeReferences(JSONArray refs) {
		ArrayList<String> rs = new ArrayList<String>();
		for (int i = 0; i < refs.length(); i++) {
			JSONObject ref = refs.getJSONObject(i);
			String sender = ref.getString("sender");
			String identifier = ref.getString("identifier");
			String sent = ref.getString("sent");
			rs.add(String.join(",", sender, identifier, sent));
		}
		return String.join(" ", rs);
	}

	/** Make parameters */
	private void makeParameters(JSONObject param, JSONObject info) {
		if (param != null) {
			Iterator<String> keys = param.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				JSONArray vals = param.getJSONArray(key);
				for (int i = 0; i < vals.length(); i++) {
					String val = vals.getString(i);
					JSONObject p = new JSONObject();
					p.putOnce("valueName", key);
					p.putOnce("value", val);
					info.append("parameter", p);
				}
			}
		}
	}

	/** Make area */
	private JSONObject makeArea(JSONObject props) {
		JSONObject area = new JSONObject();
		area.putOnce("areaDesc", props.getString("areaDesc"));
		JSONObject geocode = props.optJSONObject("geocode");
		if (geocode != null) {
			Iterator<String> keys = geocode.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				JSONArray vals = geocode.getJSONArray(key);
				for (int i = 0; i < vals.length(); i++) {
					String val = vals.getString(i);
					JSONObject p = new JSONObject();
					p.putOnce("valueName", key);
					p.putOnce("value", val);
					area.append("geocode", p);
				}
			}
		}
		// FIXME: convert GeoJSON to CAP polygon
		return area;
	}
}
