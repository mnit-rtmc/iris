/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseMultiHelper;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.LCSArrayImpl;

/**
 * Operation to query indicaitons on a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpQueryLCSIndications extends OpLCS {

	/** Current indications */
	protected final Integer[] ind_current;

	/** Queried indications */
	protected final Integer[] ind_queried;

	/** Create a new operation to send LCS indications */
	public OpQueryLCSIndications(LCSArrayImpl l) {
		super(DEVICE_DATA, l);
		ind_current = l.getIndicationsCurrent();
		ind_queried = Arrays.copyOf(ind_current, ind_current.length);
		lookupIndications();
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return null;
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(!Arrays.equals(ind_current, ind_queried))
			lcs_array.setIndicationsCurrent(ind_queried, null);
		super.cleanup();
	}

	/** Lookup the indications on the LCS array */
	protected void lookupIndications() {
		LCS[] lcss = LCSArrayHelper.lookupLCSs(lcs_array);
		if(lcss.length != ind_queried.length) {
			System.err.println("lookupIndications: array invalid");
			return;
		}
		for(int i = 0; i < lcss.length; i++) {
			DMS dms = DMSHelper.lookup(lcss[i].getName());
			SignMessage sm = dms.getMessageCurrent();
			ind_queried[i] = lookupIndication(sm);
		}
	}

	/** Lookup an indication on a sign message */
	protected Integer lookupIndication(SignMessage sm) {
		MultiString ms = new MultiString(sm.getMulti());
		if(ms.isBlank())
			return LaneUseIndication.DARK.ordinal();
		LaneUseMulti lum = LaneUseMultiHelper.find(ms);
		if(lum != null)
			return lum.getIndication();
		else
			return null;
	}
}
