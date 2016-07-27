/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.MILES;
import us.mn.state.dot.tms.units.Speed;
import static us.mn.state.dot.tms.units.Speed.Units.MPH;

/**
 * Mainline backup location finder.
 *
 * @author Douglas Lau
 */
public class BackupFinder implements Corridor.StationFinder {

	/** Speed threshold to indicate backup */
	private final Speed spd_thresh;

	/** Backup limit distance (miles) */
	private final float blimit_mi;

	/** Mile point to search for backup */
	private final float ma;

	/** Milepoint at backup station */
	private Float back_mp;

	/** Create a new backup finder.
	 * @param as Speed threshold to indicate backup.
	 * @param bd Distance limit to backup (negative indicates upstream).
	 * @param m Milepoint to start from. */
	public BackupFinder(Speed as, Distance bd, float m) {
		spd_thresh = as;
		blimit_mi = bd.asFloat(MILES);
		ma = m;
	}

	/** Check for mainline backup at a station.  From StationFinder.check.
	 * @param m Milepoint of station.
	 * @param s Station to check.
	 * @return true to stop checking (never). */
	@Override
	public boolean check(Float m, StationImpl s) {
		if (back_mp == null && isNearSearch(m)) {
			float spd = s.getRollingAverageSpeed();
			if (spd > 0 && spd < spd_thresh.round(MPH))
				back_mp = m;
		}
		return false;
	}

	/** Check if a location is near the point we're searching.
	 * @param m Milepoint of location.
	 * @return true if location is near search point. */
	private boolean isNearSearch(Float m) {
		if (m != null) {
			float d = m - ma;
			if (blimit_mi > 0)
				return d > 0 && d < blimit_mi;
			else
				return d < 0 && d > blimit_mi;
		} else
			return false;
	}

	/** Calculate the distance to mainline backup.
	 * @return Distance to end of backup, or null for no backup. */
	public Distance backupDistance() {
		if (back_mp != null) {
			float d = back_mp - ma;
			return new Distance(d, MILES);
		} else
			return null;
	}

	/** Debug the finder */
	public void debug(DebugLog slog) {
		slog.log("spd_thresh: " + spd_thresh +
		         ", blimit_mi: " + blimit_mi +
		         ", ma: " + ma +
		         ", backup: " + backupDistance());
	}
}
