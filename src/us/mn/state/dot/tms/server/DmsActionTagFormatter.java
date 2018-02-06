/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2018  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.utils.MultiAdapter;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Formatter for custom DMS action tags.  These tags are similar to MULTI, but
 * processed before sending to the sign.
 *
 * @author Douglas Lau
 */
public class DmsActionTagFormatter {

	/** DMS for message formatting */
	private final DMSImpl dms;

	/** Travel time estimator */
	private final TravelTimeEstimator travel_est;

	/** Speed advisory calculator */
	private final SpeedAdvisoryCalculator advisory;

	/** Slow warning formatter */
	private final SlowWarningFormatter slow_warn;

	/** Tolling formatter */
	private final TollingFormatter toll_form;

	/** Create a new DMS action tag formatter */
	public DmsActionTagFormatter(DMSImpl d, TollingFormatter tf) {
		dms = d;
		GeoLoc g = d.getGeoLoc();
		travel_est = new TravelTimeEstimator(dms.getName(), g);
		advisory = new SpeedAdvisoryCalculator(g);
		slow_warn = new SlowWarningFormatter(g);
		toll_form = tf;
	}

	/** Process a DMS action */
	public MultiWithSrc process(DmsAction da) {
		QuickMessage qm = da.getQuickMessage();
		return (qm != null) ? process(da, qm) : null;
	}

	/** Process a DMS action */
	private MultiWithSrc process(DmsAction da, QuickMessage qm) {
		FeedCallback fc = new FeedCallback(dms, da.getSignGroup());
		new MultiString(qm.getMulti()).parse(fc);
		String fm = fc.toString();
		String ms = createMulti(fm);
		return (ms != null)
		      ? new MultiWithSrc(ms, createSrc(fm))
		      : null;
	}

	/** Create a MULTI string for a message.
	 * @param ms MULTI string to parse.
	 * @return MULTI string with DMS action tags resolved. */
	private String createMulti(String ms) {
		MultiString multi = new MultiString(ms);
		if (multi.isBlank())
			return null;
		// FIXME: combine these into a single MULTI parse step.
		String tm = travel_est.replaceTravelTimes(ms);
		if (tm != null) {
			String am = advisory.replaceSpeedAdvisory(tm);
			if (am != null) {
				String sm = slow_warn.replaceSlowWarning(am);
				if (sm != null)
					return toll_form.replaceTolling(sm);
			}
		}
		return null;
	}

	/** Create source bit flags */
	private int createSrc(String ms) {
		int src = SignMsgSource.schedule.bit();
		if (isTravelTime(ms))
			src |= SignMsgSource.travel_time.bit();
		if (isTolling(ms))
			src |= SignMsgSource.tolling.bit();
		return src;
	}

	/** Does the MULTI string have a travel time [tt] tag? */
	private boolean isTravelTime(String ms) {
		final boolean[] travel = new boolean[] { false };
		new MultiString(ms).parse(new MultiAdapter() {
			@Override
			public void addTravelTime(String sid,
				OverLimitMode mode, String o_txt)
			{
				travel[0] = true;
			}
		});
		return travel[0];
	}

	/** Does the MULTI string have a tolling [tz] tag? */
	private boolean isTolling(String ms) {
		final boolean[] tolling = new boolean[] { false };
		new MultiString(ms).parse(new MultiAdapter() {
			@Override
			public void addTolling(String mode, String[] zones) {
				tolling[0] = true;
			}
		});
		return tolling[0];
	}
}
