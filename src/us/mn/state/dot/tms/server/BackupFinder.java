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

	/** Distance to check upstream for backup */
	static private final int UPSTREAM_DIST_MI = 2;

	/** Speed threshold to indicate backup */
	private final Speed spd_thresh;

	/** Backup limit distance (miles) */
	private final float blimit_mi;

	/** Mile point to start search */
	private final float start_mp;

	/** Milepoint at backup station */
	private Float back_mp;

	/** Backup speed */
	private float back_spd = 0;

	/** Upstream backed up */
	private boolean back_upstream = false;

	/** Create a new backup finder.
	 * @param as Speed threshold to indicate backup.
	 * @param bd Distance limit to backup.
	 * @param m Milepoint to start from. */
	public BackupFinder(Speed as, Distance bd, float m) {
		spd_thresh = as;
		blimit_mi = bd.asFloat(MILES);
		start_mp = m;
	}

	/** Check for mainline backup at a station.  From StationFinder.check.
	 * @param m Milepoint of station.
	 * @param s Station to check.
	 * @return true to stop checking (never). */
	@Override
	public boolean check(float m, StationImpl s) {
		float spd = s.getRollingAverageSpeed();
		if (spd > 0 && spd < spd_thresh.round(MPH)) {
			if (isNearUpstream(m))
				back_upstream = true;
			else if (isNearDownstream(m)) {
				if (back_mp == null || back_mp > m) {
					back_mp = m;
					back_spd = spd;
				}
			}
		}
		return false;
	}

	/** Check if a location is near and upstream.
	 * @param m Milepoint of location.
	 * @return true if location is near and upstream. */
	private boolean isNearUpstream(Float m) {
		float d = m - start_mp;
		return (d < 0) && (d > -UPSTREAM_DIST_MI);
	}

	/** Check if a location is near and downstream.
	 * @param m Milepoint of location.
	 * @return true if location is near and downstream. */
	private boolean isNearDownstream(Float m) {
		float d = m - start_mp;
		return (d > 0) && (d < blimit_mi);
	}

	/** Get the distance to mainline backup.
	 * @return Distance to end of backup, or null for no backup. */
	public Distance distance() {
		if (isBackedUp()) {
			float d = back_mp - start_mp;
			return new Distance(d, MILES);
		} else
			return null;
	}

	/** Get the speed at the backup.
	 * @return Speed at end of backup, or null for no backup. */
	public Speed speed() {
		if (isBackedUp())
			return new Speed(back_spd, Speed.Units.MPH);
		else
			return null;
	}

	/** Check if traffic is backed up */
	private boolean isBackedUp() {
		return (back_mp != null) && !back_upstream;
	}

	/** Debug the finder */
	public void debug(DebugLog slog) {
		slog.log("spd_thresh: " + spd_thresh +
		         ", blimit_mi: " + blimit_mi +
		         ", start_mp: " + start_mp +
		         ", dist: " + distance() +
		         ", spd: " + speed());
	}
}
