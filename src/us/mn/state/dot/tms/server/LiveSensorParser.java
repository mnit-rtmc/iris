/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026  Minnesota Department of Transportation
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Parser for JSON live sensor data from pollinator service.
 *
 * @author Douglas Lau
 */
public class LiveSensorParser {

	/** JSON sensor data file */
	static private final String SENSOR_JSON =
		"/var/lib/iris/web/sensor_data";

	/** JSON sensor data debug log */
	static public final DebugLog LOG = new DebugLog("sensor_json");

	/** Date formatter for RFC 3339 */
	static private final String RFC3339 = "yyyy-MM-dd'T'HH:mm:ssXXX";

	/** Parse a date/time stamp */
	static private long parseStamp(String v) throws ParsingException {
		try {
			return new SimpleDateFormat(RFC3339).parse(v).getTime();
		}
		catch (ParseException e) {
			throw new ParsingException(e);
		}
	}

	/** Read a file into a string, in UTF-8 encoding */
	static private String readFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		return new String(bytes, StandardCharsets.UTF_8);
	}

	/** Time stamp from file */
	private final long stamp;

	/** Data collection period (sec) */
	private final int period;

	/** Read live sensor data from pollinator file */
	public LiveSensorParser(long st) throws IOException, JSONException {
		String doc;
		try {
			doc = readFile(SENSOR_JSON);
		}
		catch (IOException e) {
			LOG.log("IO Error: " + e.getMessage());
			throw e;
		}
		try {
			JSONObject jo = new JSONObject(doc);
			stamp = parseStamp(jo.getString("time_stamp"));
			period = jo.getInt("period");
			long p = period * 1000;
			long stamp_end = stamp / p * p;
			if (stamp_end == st)
				parseSensorData(jo);
			else {
				LOG.log("Invalid stamp: " + stamp + ", " + st);
			}
		}
		catch (JSONException e) {
			LOG.log("JSON Error: " + e.getMessage());
			throw e;
		}
	}

	/** Parse live sensor data */
	private void parseSensorData(JSONObject jo) throws JSONException {
		JSONObject samples = jo.getJSONObject("samples");
		Iterator<String> keys = samples.keys();
		while (keys.hasNext()) {
			String sid = keys.next();
			Detector det = DetectorHelper.lookup(sid);
			if (det instanceof DetectorImpl) {
				storeSensorData((DetectorImpl) det,
					samples.getJSONArray(sid));
			} else {
				LOG.log("Unknown sensor: " + sid);
			}
		}
	}

	/** Store sensor data */
	private void storeSensorData(DetectorImpl det, JSONArray values) {
		Integer flow = values.optInt(0);
		if (flow != null) {
			int count = (flow * period) / 3600;
			PeriodicSample ps = new PeriodicSample(stamp, period,
				count);
			det.storeVehCount(ps, true);
		}
		Integer speed = values.optInt(1);
		if (speed != null) {
			PeriodicSample ps = new PeriodicSample(stamp, period,
				speed);
			det.storeSpeed(ps, true);
		}
	}
}
