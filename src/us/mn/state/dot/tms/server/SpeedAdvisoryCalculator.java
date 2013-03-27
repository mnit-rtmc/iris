/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.MultiParser;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Speed Advisory Calculator
 *
 * @author Douglas Lau
 */
public class SpeedAdvisoryCalculator {

	/** VSA debug log */
	static private final DebugLog VSA_LOG = new DebugLog("vsa");

	/** Get the minimum speed to display for advisory */
	static private int getMinDisplay() {
		return SystemAttrEnum.VSA_MIN_DISPLAY_MPH.getInt();
	}

	/** Get the maximum speed to display for advisory */
	static private int getMaxDisplay() {
		return SystemAttrEnum.VSA_MAX_DISPLAY_MPH.getInt();
	}

	/** Round up to the nearest 5 mph */
	static private int round5Mph(float mph) {
		return Math.round(mph / 5) * 5;
	}

	/** Location for advisory */
	protected final GeoLoc loc;

	/** Create a new speed advisory calculator */
	public SpeedAdvisoryCalculator(GeoLoc l) {
		loc = l;
	}

	/** Replace speed advisory tags in a MULTI string */
	public String replaceSpeedAdvisory(String multi) {
		MultiCallback cb = new MultiCallback();
		MultiParser.parse(multi, cb);
		if(cb.valid)
			return cb.toString();
		else
			return null;
	}

	/** MultiString for replacing speed advisory tags */
	protected class MultiCallback extends MultiString {

		protected boolean valid = true;

		/** Add a variable speed advisory */
		public void addSpeedAdvisory() {
			Integer a = calculateSpeedAdvisory();
			if(a != null)
				addSpan(String.valueOf(a));
			else
				valid = false;
		}
	}

	/** Calculate the speed advisory */
	private Integer calculateSpeedAdvisory() {
		String c = GeoLocHelper.getCorridorName(loc);
		if(c != null)
			return calculateSpeedAdvisory(c);
		else
			return null;
	}

	/** Calculate the speed advisory */
	private Integer calculateSpeedAdvisory(String c) {
		Corridor cor = BaseObjectImpl.corridors.getCorridor(c);
		if(cor != null) {
			Float m = cor.calculateMilePoint(loc);
			if(VSA_LOG.isOpen())
				VSA_LOG.log(loc.getName() + ", mp: " + m);
			if(m != null)
				return calculateSpeedAdvisory(cor, m);
		}
		return null;
	}

	/** Calculate the speed advisory */
	private Integer calculateSpeedAdvisory(Corridor cor, float m) {
		VSStationFinder vss_finder = new VSStationFinder(m);
		cor.findStation(vss_finder);
		if(VSA_LOG.isOpen())
			vss_finder.debug(VSA_LOG);
		if(vss_finder.foundVSS()) {
			Integer lim = vss_finder.getSpeedLimit();
			if(lim != null) {
				Float a = vss_finder.calculateSpeedAdvisory();
				if(a != null) {
					a = Math.max(a, getMinDisplay());
					int sa = round5Mph(a);
					if(sa < lim && sa <= getMaxDisplay())
						return sa;
					else
						return null;
				}
			}
		}
		return null;
	}
}
