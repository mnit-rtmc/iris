/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.MultiParser;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.QuickMessage;

/**
 * MULTI string formatter for custom tags.
 *
 * @author Douglas Lau
 */
public class MultiFormatter {

	/** DMS for message formatting */
	private final DMSImpl dms;

	/** Travel time estimator */
	private final TravelTimeEstimator travel_est;

	/** Speed advisory calculator */
	private final SpeedAdvisoryCalculator advisory;

	/** Slow warning formatter */
	private final SlowWarningFormatter slow_warn;

	/** Create a new MULTI formatter */
	public MultiFormatter(DMSImpl d) {
		dms = d;
		GeoLoc g = d.getGeoLoc();
		travel_est = new TravelTimeEstimator(dms.getName(), g);
		advisory = new SpeedAdvisoryCalculator(g);
		slow_warn = new SlowWarningFormatter(g);
	}

	/** Create a multi string for a DMS action */
	public String createMulti(DmsAction da) {
		QuickMessage qm = da.getQuickMessage();
		if(qm != null) {
			FeedCallback fc = new FeedCallback(dms,
				da.getSignGroup());
			MultiParser.parse(qm.getMulti(), fc);
			String m = fc.toString();
			MultiString multi = new MultiString(m);
			if(!multi.isBlank())
				return createMulti(m);
		}
		return null;
	}

	/** Create a MULTI string for a message.
	 * @param qm Quick message MULTI string to parse.
	 * @return MULTI string with travel, vsa and slow warnings resolved. */
	private String createMulti(String qm) {
		// FIXME: combine these into a single MULTI parse step.
		String tm = travel_est.replaceTravelTimes(qm);
		if(tm != null) {
			String am = advisory.replaceSpeedAdvisory(tm);
			if(am != null)
				return slow_warn.replaceSlowWarning(am);
		}
		return null;
	}

	/** Clear the current routes */
	public void clear() {
		travel_est.clear();
	}
}
