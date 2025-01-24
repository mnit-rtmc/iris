/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2022  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.clearguide;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONArray;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Synchronized collection of ClearGuide DMS and associated data.
 *
 * @author Michael Darter
 */
public class DmsContainer {

	/** Write a log message */
	static private void log(String msg) {
		ClearGuidePoller.slog("DmsContainer." + msg);
	}

	/** Get null-safe string */
	static private String safe(String str) {
		return (str != null ? str : "");
	}

	/** Get current time in ms */
	static private long now() {
		return System.currentTimeMillis();
	}

	/** System creation time */
	private final Date create_time;

	/** Hash of DMS to associated routes */
	private final ConcurrentHashMap<String,Routes> dms_hash =
		new ConcurrentHashMap<String,Routes>();

	/** Constructor */
	protected DmsContainer() {
		create_time = new Date();
	}

	/* Return number of key value pairs in hash */
	protected int size() {
		return dms_hash.size();
	}

	/* Clear container contents */
	private void clear() {
		dms_hash.clear();
	}

	/** Add JSON encoded DMS and associated data to container.
	 * @param json DMS encoded as: { "dms_name": [{...route obj...},{...}]}
	 * @return Number of DMS in container */
	protected int add(String json) throws ParsingException {
		json = safe(json);
		log("parse: json_len=" + json.length());
		log("parse: json=" + json);
		JSONObject top = new JSONObject(json);
		// for each dms: name, [{dms_attributes}, {}, ...]
		for (String dms : top.keySet()) {
			JSONArray jsonroutes = top.getJSONArray(dms);
			Routes rs = new Routes();
			if (rs.parse(jsonroutes) > 0) { // ParsingException
				addDms(dms, rs);
				log("parse: dms=" + dms + " routes=" + rs);
			}
		}
		return size();
	}

	/** Add a DMS and associated Routes */
	private int addDms(String dms, Routes rs) {
		dms_hash.put(dms, rs);
		return size();
	}

	/** Get the specified statistic for the specified dms.
	 * @param dms DMS to retrieve statistic for.
	 * @param rid Route id
	 * @param min Min statistic value from [cg] tag, 0 to ignore.
	 * @param mode Statistic to retrieve as defined by [cg] tag or null.
	 * @param ridx Route index, zero based.
	 * @return Specified statistic or null if not found */
	public Integer getStat(
		String dms, int rid, int min, String mode, int ridx)
	{
		log("getStat: dms=" + dms + " rid=" + rid + " min=" + min +
			" mode=" + mode + " contains_dms=" +
			dms_hash.containsKey(dms));
		Integer stat = null;
		Routes routes = dms_hash.get(dms);
		if (routes != null && routes.size() >= 1) {
			if (routes.size() > 1)
				log("getStat: dms n_routes=" + routes.size());
			stat = routes.getStat(rid, min, mode, ridx);
			if (stat == null) {
				log("getStat: mismatch rid=" + rid +
				    " mode=" + mode);
			}
		}
		return stat;
	}

	/** To string */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(dms_container:");
		sb.append(" create_time=").append(create_time);
		sb.append(" size=").append(size());
		for (Map.Entry<String,Routes> dms : dms_hash.entrySet()) {
			String dmsname = dms.getKey();
			Routes routes = dms.getValue();
			sb.append(" (DMS=").append(dmsname);
			sb.append(" Routes=").append(routes);
			sb.append(")");
		}
		sb.append(")");
		return sb.toString();
	}
}
