/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query indicaitons on a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpQueryLCSIndications extends OpLCS {

	/** Create a new operation to send LCS indications */
	public OpQueryLCSIndications(LCSArrayImpl l) {
		super(PriorityLevel.DEVICE_DATA, l);
		if (l.isQueryAllowed())
			lookupIndications();
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return null;
	}

	/** Lookup the indications on the LCS array */
	private void lookupIndications() {
		for (int i = 0; i < dmss.length; i++) {
			DMSImpl dms = dmss[i];
			if (dms != null)
				ind_after[i] = lookupIndication(dms);
		}
	}

	/** Lookup an indication on a DMS */
	private Integer lookupIndication(DMSImpl dms) {
		if (dms.isFailed() || DMSHelper.hasCriticalError(dms))
			return null;
		else {
			SignMessage sm = dms.getMsgCurrent();
			return lookupIndication(sm);
		}
	}
}
