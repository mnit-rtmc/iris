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

import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.LcsIndication;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LcsImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query indicaitons on a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpQueryLCSIndications extends OpLCS {

	/** Create a new operation to send LCS indications */
	public OpQueryLCSIndications(LcsImpl l) {
		super(PriorityLevel.POLL_LOW, l);
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
		for (int ln = 0; ln < dmss.length; ln++)
			ind_after[ln] = lookupIndication(dmss[ln]);
	}

	/** Lookup an indication on a DMS */
	private int lookupIndication(DMSImpl dms) {
		if (dms.isDeployable()) {
			SignMessage sm = dms.getMsgCurrent();
			return (sm != null)
			      ? lookupIndication(sm)
			      : LcsIndication.UNKNOWN.ordinal();
		} else
			return LcsIndication.UNKNOWN.ordinal();
	}
}
