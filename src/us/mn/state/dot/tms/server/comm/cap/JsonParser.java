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
package us.mn.state.dot.tms.server.comm.cap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Common Alerting Protocol (CAP) JSON document parser.
 *
 * Parses NWS "CAP" LD-JSON documents and fixes up quirks.
 *
 * @author Douglas Lau
 */
public class JsonParser implements AlertParser {

	/** Parse alerts */
	@Override
	public List<JSONObject> parse(String doc) throws IOException {
		try {
			JSONObject jo = new JSONObject(doc);
		}
		catch (JSONException e) {
			throw new ParsingException(e);
		}
		return new ArrayList<JSONObject>();
	}
	
	/*	JSONArray jof = jo.getJSONArray("features");
		for (int i = 0; i < jof.length(); i++) {
			JSONObject jf = jof.getJSONObject(i).getJSONObject("properties");
			PROCESSOR.processAlert(jf);
		}*/

	/*
		String id = null;
		if (ja.has("identifier"))
			id = ja.getString("identifier");
		else if (ja.has("id"))
			id = ja.getString("id");
			*/

	/*
		String msgType = alert.has("msgType")
				? alert.getString("msgType")
				: alert.getString("messageType");
		CapMsgType msg_type = CapMsgType.fromValue(msgType);
	*/
}
