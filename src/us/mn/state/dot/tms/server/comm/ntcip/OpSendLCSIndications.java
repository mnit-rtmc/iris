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

import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseMultiHelper;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;

/**
 * Operation to send new indicaitons to a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpSendLCSIndications extends OpLCS {

	/** Get the activation priority for the specified indication */
	static protected DMSMessagePriority getActivationPriority(int ind) {
		switch(LaneUseIndication.fromOrdinal(ind)) {
		case LANE_CLOSED:
			return DMSMessagePriority.INCIDENT_HIGH;
		case LANE_CLOSED_AHEAD:
		case MERGE_RIGHT:
		case MERGE_LEFT:
		case MERGE_BOTH:
		case MUST_EXIT_RIGHT:
		case MUST_EXIT_LEFT:
			return DMSMessagePriority.INCIDENT_MED;
		case USE_CAUTION:
			return DMSMessagePriority.INCIDENT_LOW;
		default:
			return DMSMessagePriority.OPERATOR;
		}
	}

	/** Indications to send */
	protected final Integer[] indications;

	/** User who sent the indications */
	protected final User user;

	/** Create a new operation to send LCS indications */
	public OpSendLCSIndications(LCSArrayImpl l, Integer[] ind, User u) {
		super(DEVICE_DATA, l);
		indications = ind;
		user = u;
		sendIndications();
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return null;
	}

	/** Send new indications to an LCS array */
	protected void sendIndications() {
		LCS[] lcss = LCSArrayHelper.lookupLCSs(lcs_array);
		if(lcss.length != indications.length) {
			System.err.println("sendIndications: array invalid");
			return;
		}
		for(int i = 0; i < lcss.length; i++) {
			DMS dms = DMSHelper.lookup(lcss[i].getName());
			if(dms instanceof DMSImpl)
				sendIndication((DMSImpl)dms, i);
		}
	}

	/** Send an indication to a DMS */
	protected void sendIndication(DMSImpl dms, int lane) {
		int ind = indications[lane];
		String ms = createIndicationMulti(dms, ind);
		if(ms != null) {
			// NOTE: this is a *slow* operation, because it has to
			//       schedule a job on the SONAR task processor
			//       thread, which might have a queue of tasks
			//       already pending.
			SignMessage sm = dms.createMessage(ms,
				getActivationPriority(ind));
			if(dms.shouldActivate(sm)) {
				try {
					dms.doSetMessageNext(sm, user);
					ind_after[lane] = ind;
				}
				catch(TMSException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** Create a MULTI string for a lane use indication */
	protected String createIndicationMulti(DMS dms, int ind) {
		String m = "";
		LaneUseMulti lum = LaneUseMultiHelper.find(ind);
		if(lum != null)
			m = lum.getMulti();
		if(m.length() > 0 ||
		   LaneUseIndication.fromOrdinal(ind) == LaneUseIndication.DARK)
			return m;
		else
			return null;
	}
}
