/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.util.Arrays;
import java.util.Iterator;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsHelper;
import us.mn.state.dot.tms.LcsIndication;
import us.mn.state.dot.tms.LcsState;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LcsImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * An LCS array operation.
 *
 * @author Douglas Lau
 */
abstract public class OpLCS extends OpNtcip {

	/** LCS array to query */
	protected final LcsImpl lcs;

	/** Indications before operation */
	protected final int[] ind_before;

	/** Indications after operation */
	protected final int[] ind_after;

	/** DMS corresponsing to each lane in the array */
	protected final DMSImpl[] dmss;

	/** Create a new LCS operation */
	protected OpLCS(PriorityLevel p, LcsImpl l) {
		super(p, l);
		lcs = l;
		ind_before = LcsHelper.getIndications(l);
		ind_after = Arrays.copyOf(ind_before, ind_before.length);
		dmss = lookupDMSs();
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			lcs.setIndicationsNotify(ind_after);
		for (DMSImpl dms: dmss) {
			if (dms == null || dms.isOffline())
				setFailed();
		}
		super.cleanup();
	}

	/** Lookup DMSs for an LCS array */
	private DMSImpl[] lookupDMSs() {
		int n_lanes = LcsHelper.countLanes(lcs);
		DMSImpl[] d_lanes = new DMSImpl[n_lanes];
		LcsState[] states = LcsHelper.lookupStates(lcs);
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS d = it.next();
			if (d instanceof DMSImpl) {
				if (!lookupLane((DMSImpl) d, states, d_lanes))
					return new DMSImpl[0];
			}
		}
		for (int i = 0; i < n_lanes; i++) {
			if (null == d_lanes[i]) {
				logError("lookupDMS: lookup failed!");
				return new DMSImpl[0];
			}
		}
		return d_lanes;
	}

	/** Lookup DMS for LCS states */
	private boolean lookupLane(final DMSImpl d, LcsState[] states,
		DMSImpl[] d_lanes)
	{
		final Controller c = d.getController();
		final int pin = d.getPin();
		for (int i = 0; i < states.length; i++) {
			LcsState ls = states[i];
			if (ls.getController() == c && ls.getPin() == pin) {
				int ln = ls.getLane() - 1;
				if (ln < 0 || ln >= d_lanes.length) {
					logError("lookupLane: bad lane " + ln);
					return false;
				}
				DMSImpl dl = d_lanes[ln];
				if (null == dl) {
					d_lanes[ln] = d;
				} else if (d != dl) {
					logError("lookupLane: mismatch " + ln);
					return false;
				}
			}
		}
		return true;
	}

	/** Lookup an LCS indication on a sign message */
	protected int lookupIndication(SignMessage sm) {
		String ms = sm.getMulti();
		if (new MultiString(ms).isBlank())
			return LcsIndication.DARK.ordinal();
		LcsState ls = findState(ms);
		return (ls != null)
		      ? ls.getIndication()
		      : LcsIndication.UNKNOWN.ordinal();
	}

	/** Find LCS state which matches a MULTI string */
	private LcsState findState(String ms) {
		LcsState[] states = LcsHelper.lookupStates(lcs);
		for (int i = 0; i < states.length; i++) {
			LcsState ls = states[i];
			MsgPattern pat = ls.getMsgPattern();
			if (pat != null && match(pat, ms))
				return ls;
		}
		return null;
	}
}
