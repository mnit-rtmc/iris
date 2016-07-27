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
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.units.Speed;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Slow Warning Formatter
 *
 * @author Douglas Lau
 */
public class SlowWarningFormatter {

	/** SLOW debug log */
	static private final DebugLog SLOW_LOG = new DebugLog("slow");

	/** Check if debug log is open */
	private boolean isLogging() {
		return SLOW_LOG.isOpen();
	}

	/** Log a debug message */
	public void log(String msg) {
		SLOW_LOG.log(loc.getName() + ": " + msg);
	}

	/** Location for warning */
	private final GeoLoc loc;

	/** Create a new slow warning formatter.
	 * @param l Location of sign. */
	public SlowWarningFormatter(GeoLoc l) {
		loc = l;
	}

	/** Replace slow warning tags in a MULTI string.
	 * @param multi MULTI string to parse.
	 * @return MULTI string with slow warning tags replaced. */
	public String replaceSlowWarning(String multi) {
		MultiCallback cb = new MultiCallback();
		new MultiString(multi).parse(cb);
		if (cb.valid)
			return cb.toString();
		else
			return null;
	}

	/** MultiBuilder for replacing slow warning tags */
	private class MultiCallback extends MultiBuilder {

		protected boolean valid = true;

		/** Add a slow warning */
		@Override
		public void addSlowWarning(int spd, int b, String units,
			boolean dist)
		{
			Speed as = createSpeed(spd, units);
			Distance bd = new Distance(b, as.units.d_units);
			Distance d = slowWarningDistance(as, bd);
			if (d != null) {
				if (dist)
					addSlowWarning(d);
			} else
				valid = false;
		}

		/** Add a slow warning */
		private void addSlowWarning(Distance d) {
			int di = d.round(d.units);
			if (di > 0)
				addSpan(String.valueOf(di));
			else
				valid = false;
		}
	}

	/** Create a speed.
	 * @param v Speed value.
	 * @param units Speed units (mph or kph).
	 * @return Matching speed. */
	private Speed createSpeed(int v, String units) {
		if (units.equals("kph"))
			return new Speed(v, Speed.Units.KPH);
		else
			return new Speed(v, Speed.Units.MPH);
	}

	/** Calculate the slow warning distance.
	 * @param as Speed to activate slow warning.
	 * @param bd Distance limit to backup (negative indicates upstream).
	 * @return Distance to backup or null for no backup. */
	private Distance slowWarningDistance(Speed as, Distance bd) {
		Corridor cor = lookupCorridor();
		if (cor != null)
			return slowWarningDistance(cor, as, bd);
		else
			return null;
	}

	/** Lookup the corridor for the given location.
	 * @return Freeway corridor. */
	private Corridor lookupCorridor() {
		return BaseObjectImpl.corridors.getCorridor(loc);
	}

	/** Estimate the slow warning distance.
	 * @param cor Freeway corridor.
	 * @param as Speed to activate slow warning.
	 * @param bd Distance limit to backup (negative indicates upstream).
	 * @return Distance to backup or null for no backup. */
	private Distance slowWarningDistance(Corridor cor, Speed as,
		Distance bd)
	{
		Float m = cor.calculateMilePoint(loc);
		if (isLogging())
			log("mp " + m);
		if (m != null)
			return slowWarningDistance(cor, as, bd, m);
		else
			return null;
	}

	/** Estimate the distance to backup.
	 * @param cor Freeway corridor.
	 * @param as Speed to activate slow warning.
	 * @param bd Distance limit to backup (negative indicates upstream).
	 * @param m Milepoint to start from.
	 * @return Distance to congestion backup, or null. */
	private Distance slowWarningDistance(Corridor cor, Speed as,
		Distance bd, float m)
	{
		BackupFinder backup_finder = new BackupFinder(as, bd, m);
		cor.findStation(backup_finder);
		if (isLogging())
			backup_finder.debug(SLOW_LOG);
		return backup_finder.backupDistance();
	}
}
