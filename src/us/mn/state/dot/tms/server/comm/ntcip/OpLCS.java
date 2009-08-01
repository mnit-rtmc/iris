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
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;

/**
 * An LCS array operation.
 *
 * @author Douglas Lau
 */
abstract public class OpLCS extends OpDevice {

	/** LCS array to query */
	protected final LCSArrayImpl lcs_array;

	/** Indications before operation */
	protected final Integer[] ind_before;

	/** Indications after operation */
	protected final Integer[] ind_after;

	/** DMS corresponsing to each LCS in the array */
	protected final DMSImpl[] dmss;

	/** Create a new LCS operation */
	protected OpLCS(int p, LCSArrayImpl l) {
		super(p, l);
		lcs_array = l;
		ind_before = l.getIndicationsCurrent();
		ind_after = Arrays.copyOf(ind_before, ind_before.length);
		dmss = new DMSImpl[ind_before.length];
		lookupDMSs();
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(!Arrays.equals(ind_before, ind_after))
			lcs_array.setIndicationsCurrent(ind_after, null);
		for(DMSImpl dms: dmss) {
			if(dms == null || dms.isFailed())
				success = false;
		}
		super.cleanup();
	}

	/** Lookup DMSs for an LCS array */
	protected void lookupDMSs() {
		LCS[] lcss = LCSArrayHelper.lookupLCSs(lcs_array);
		if(lcss.length != ind_before.length) {
			System.err.println("lookupDMS: array invalid");
			return;
		}
		for(int i = 0; i < lcss.length; i++) {
			DMS dms = DMSHelper.lookup(lcss[i].getName());
			if(dms instanceof DMSImpl)
				dmss[i] = (DMSImpl)dms;
		}
	}
}
