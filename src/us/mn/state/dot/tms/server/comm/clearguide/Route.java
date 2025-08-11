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

import org.json.JSONObject;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * ClearGuide Route and associated data.
 *
 * @author Michael Darter
 */
public class Route {

	/** Maximum age of ClearGuide data before ignored */
	static final int MAX_CG_DATA_AGE_SECS = 6 * 60;

	/** Write a log message */
	static private void log(String msg) {
		ClearGuidePoller.slog("Route." + msg);
	}

	/** Get current time in ms */
	static private long now() {
		return System.currentTimeMillis();
	}

	/** Get time delta in secs */
	static private long timeDeltaSecs(long start_ms) {
		return (now() - start_ms) / 1000L;
	}

	/** ClearGuide Route id */
	protected final int gcr_id;

	/** ClearGuide route travel time actual (mins) */
	protected final int gcr_tta;

	/** ClearGuide route travel time at the speed limit (mins) */
	protected final int gcr_ttsl;

	/** ClearGuide route delay (mins) */
	protected final int gcr_delay;

	/** ClearGuide route datetime in ms */
	protected final long gcr_time;

	/** ClearGuide route speed in mph */
	protected final int gcr_speed;

	/** Create a route from JSON.
	 * @param jroute A single route with attributes:
	 *       {"route_id":34731,"speed_mph":68,"route_name":"Cliff to
	 *       Diffley Que anal","travel_time_mins":1,"delay_ff_mins":0,
	 *       "delay_sl_mins":0,"timestamp":1.611017443E9} */
	protected Route(JSONObject jroute) throws ParsingException {
		this(
			jroute.getInt("route_id"),
			jroute.getDouble("travel_time_mins"),
			jroute.getDouble("delay_sl_mins"),
			jroute.getLong("timestamp") * 1000,
			jroute.getDouble("sl_travel_time_mins"),
			jroute.getInt("speed_mph")
		);
	}

	/** Create a route with CG calculated values
 	 * @param rid ClearGuide route id, 0 to ignore.
	 * @param rtta Calculated route travel time actual
	 * @param rd Calculated workzone delay
	 * @param rt Route time Unix time in ms
	 * @param rttsl Calculated route travel time at speed limit
	 * @param rsp Calculated route speed in mph */
	private Route(
		int rid, double rtta, double rd, long rt, double rttsl, int rsp) 
	{
		gcr_id = rid;
		gcr_tta = (int)Math.round(rtta);
		gcr_delay = (int)Math.round(rd);
		gcr_time = rt;
		gcr_ttsl = (int)Math.round(rttsl);
		gcr_speed = rsp;
	}

	/** Get age of data in secs */
	private long getAgeSecs() {
		return timeDeltaSecs(gcr_time);
	}

	/** Get travel time adjusted by the speed limit */
	private int getTravelTime() {
		return Math.max(gcr_tta, gcr_ttsl);
	}

	/** Get ClearGuide calculated statistic by matching [cg] tag values.
 	 * @param wid ClearGuide workzone ID from [cg] tag.
	 * @param mode Statistic defined by [cg] tag.
	 * @return Statistic from CG server or null for missing */
	protected Integer getStat(int wid, ModeEnum mode) {
		final Integer stat;
		if (getAgeSecs() > MAX_CG_DATA_AGE_SECS) {
			stat = null;
			log("getStat: CG data too old: age_s=" + getAgeSecs() +
				" > " + MAX_CG_DATA_AGE_SECS);
		} else if (wid != gcr_id) {
			stat = null;
			log("getStat: cg tag route id " + wid + " no match");
		} else if (mode == ModeEnum.DELAY) {
			stat = gcr_delay;
		} else if (mode == ModeEnum.TRAVELTIME) {
			stat = getTravelTime();
		} else if (mode == ModeEnum.TRAVELTIME_ACTUAL) {
			stat = gcr_tta;
		} else if (mode == ModeEnum.TRAVELTIME_SPEED_LIMIT) {
			stat = gcr_ttsl;
		} else if (mode == ModeEnum.SPEED) {
			stat = gcr_speed;
		} else if (mode == ModeEnum.SPEED_CONDITION) {
			stat = gcr_speed;
		} else {
			stat = null;
			log("getStat: unaccounted mode");
		}
		log("getStat: wid=" + wid + " mode=" + mode +
			" route=" + toString() + " --> stat=" + stat);
		return stat;
	}

	/** Get statistic from string mode */
	protected Integer getStat(int wid, String mode) {
		return getStat(wid, ModeEnum.fromValue(mode));
	}

	/* To string */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(Route: id=").append(gcr_id);
		sb.append(" tta_m=").append(gcr_tta);
		sb.append(" ttsl_m=").append(gcr_ttsl);
		sb.append(" tt_m=").append(getTravelTime());
		sb.append(" speed=").append(gcr_speed);
		sb.append(" delay_m=").append(gcr_delay);
		sb.append(" time=").append(gcr_time);
		sb.append(" age_s=").append(getAgeSecs());
		sb.append(")");
		return sb.toString();
	}
}
