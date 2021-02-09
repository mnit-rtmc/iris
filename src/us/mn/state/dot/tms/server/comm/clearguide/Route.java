/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Iteris Inc.
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

	/** ClearGuide route travel time (mins) */
	protected final int gcr_tt;

	/** ClearGuide route delay (mins) */
	protected final int gcr_delay;

	/** ClearGuide route datetime in ms */
	protected final long gcr_time;

	/** Create a route with CG calculated values
 	 * @param rid ClearGuide route id, 0 to ignore.
	 * @param rtt Calculated route travel time
	 * @param rd Calculated workzone delay
	 * @param rt Route time Unix time in ms */
	protected Route(int rid, double rtt, double rd, long rt) {
		gcr_id = rid;
		gcr_tt = (int)Math.round(rtt);
		gcr_delay = (int)Math.round(rd);
		gcr_time = rt;
	}

	/** Get age of data in secs */
	private long getAgeSecs() {
		return timeDeltaSecs(gcr_time);
	}

	/** Get ClearGuide calculated statistic by matching [cg] tag values.
 	 * @param rid ClearGuide route id from [cg] tag.
 	 * @param min Min statistic value from [cg] tag, 0 to ignore.
	 * @param mode Statistic defined by [cg] tag.
	 * @return Statistic from CG server or null for missing */
	protected Integer getStat(int rid, int min, ModeEnum mode) {
		Integer stat;
		if (getAgeSecs() > MAX_CG_DATA_AGE_SECS) {
			stat = null;
			log("getStat: CG data too old: age_s=" + getAgeSecs() +
				" > " + MAX_CG_DATA_AGE_SECS);
		} else if (mode == ModeEnum.UNKNOWN) {
			stat = null;
			log("getStat: unknown cg tag mode");
		} else if (rid != gcr_id) {
			stat = null;
			log("getStat: cg tag route id " + rid + " no match");
		} else if (mode == ModeEnum.DELAY) {
			stat = (gcr_delay >= min ? gcr_delay : null);
			if (stat == null)
				log("getStat: delay less than minimum");
		} else if (mode == ModeEnum.TRAVELTIME) {
			stat = (gcr_tt >= min ? gcr_tt : null);
			if (stat == null)
				log("getStat: tt less than minimum");
		} else {
			stat = null;
			log("getStat: unknown mode");
		}
		log("getStat: rid=" + rid + " mode=" + mode +
			" route=" + toString() + " -->stat=" + stat);
		return stat;
	}

	/** Get statistic from string mode */
	protected Integer getStat(int rid, int min, String mode) {
		return getStat(rid, min, ModeEnum.fromValue(mode));
	}

	/* To string */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(Route: id=").append(gcr_id);
		sb.append(" tt_m=").append(gcr_tt);
		sb.append(" delay_m=").append(gcr_delay);
		sb.append(" time=").append(gcr_time);
		sb.append(" age_s=").append(getAgeSecs());
		sb.append(")");
		return sb.toString();
	}
}
