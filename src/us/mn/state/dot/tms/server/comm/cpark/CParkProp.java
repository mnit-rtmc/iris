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
package us.mn.state.dot.tms.server.comm.cpark;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.sched.TimeSteward;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Available spots property.
 *
 * @author Douglas Lau
 */
public class CParkProp extends ControllerProperty {

	/** Time stamp */
	private final long stamp;

	/** Get timestamp at end of interval */
	public long getTime() {
		return stamp;
	}

	/** Sample perdiod */
	private final int per_sec;

	/** Get the interval period (sec) */
	public int getPeriod() {
		return per_sec;
	}

	/** Parking spot occupancy array */
	private final int[] scans = new int[64];

	/** Get parking spot occupancy array */
	public int[] getScans() {
		return scans;
	}

	/** Create an available spots property */
	public CParkProp(int p) {
		stamp = TimeSteward.currentTimeMillis();
		per_sec = p;
		for (int i = 0; i < scans.length; i++)
			scans[i] = MISSING_DATA;
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		try {
			JSONObject jo = new JSONObject(parseResp(is));
			for (int i = 0; i < 64; i++) {
				String key = String.valueOf(i + 1);
				JSONObject spot = jo.optJSONObject(key);
				if (spot != null)
					parseSpot(i, spot);
			}
		}
		catch (JSONException e) {
			throw new ParsingException("JSON: " + e.getMessage());
		}
	}

	/** Parse parking spot object */
	private void parseSpot(int i, JSONObject spot) {
		try {
			boolean available = spot.getBoolean("available");
			scans[i] = (available) ? 0 : 1;
			CParkPoller.slog("spot:" + i + ", " + available);
			int duration = spot.getInt("duration");
			CParkPoller.slog("duration: " + duration);
		}
		catch (JSONException e) {
			// spot not defined
		}
	}

	/** Parse a response */
	private String parseResp(InputStream is) throws IOException {
		char[] buf = new char[1024];
		InputStreamReader isr = new InputStreamReader(is);
		StringBuilder res = new StringBuilder();
		for (int i = 0;; i++) {
			int n = isr.read(buf, 0, 1024);
			if (n < 0)
				break;
			else if (n > 0)
				res.append(buf, 0, n);
			if (i > 100)
				throw new ParsingException("RESP TOO BIG");
		}
		return res.toString();
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "spots";
	}
}
