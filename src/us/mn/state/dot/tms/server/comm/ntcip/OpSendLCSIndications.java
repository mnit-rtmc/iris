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

import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMSMessagePriority;
import static us.mn.state.dot.tms.DMSMessagePriority.*;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseMultiHelper;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignMessage;
import static us.mn.state.dot.tms.SignMsgSource.lcs;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.utils.MultiString;

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
			return INCIDENT_HIGH;
		case LANE_CLOSED_AHEAD:
		case MERGE_RIGHT:
		case MERGE_LEFT:
		case MERGE_BOTH:
		case MUST_EXIT_RIGHT:
		case MUST_EXIT_LEFT:
			return INCIDENT_MED;
		case USE_CAUTION:
			return INCIDENT_LOW;
		default:
			return OPERATOR;
		}
	}

	/** Indications to send */
	protected final Integer[] indications;

	/** Sign messages for each DMS in the LCS array */
	protected final SignMessage[] msgs;

	/** Create a new operation to send LCS indications */
	public OpSendLCSIndications(LCSArrayImpl l, Integer[] ind, User u) {
		super(PriorityLevel.DEVICE_DATA, l, u);
		indications = ind;
		msgs = new SignMessage[ind.length];
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return new CreateSignMessages();
	}

	/** Phase to create sign messages */
	protected class CreateSignMessages extends Phase {

		/** Lane of DMS for sign message */
		protected int lane = 0;

		/** Create a sign message */
		protected Phase poll(CommMessage mess) {
			if(lane < msgs.length) {
				msgs[lane] = createSignMessage(lane);
				lane++;
				return this;
			} else
				return new SendMessages();
		}
	}

	/** Create a sign message */
	protected SignMessage createSignMessage(int lane) {
		int ind = indications[lane];
		DMSImpl dms = dmss[lane];
		if (dms != null) {
			Integer w = dms.getWidthPixels();
			Integer h = dms.getHeightPixels();
			if (w == null || h == null)
				return null;
			String ms = createIndicationMulti(ind, w, h);
			if (ms != null)
				return createSignMessage(dms, ms, ind);
		}
		return null;
	}

	/** Create a sign message.
	 * This is a *slow* operation, because it has to schedule a job on the
	 * SONAR task processor thread, which might have a queue of tasks
	 * already pending. */
	private SignMessage createSignMessage(DMSImpl dms, String ms, int ind) {
		DMSMessagePriority ap = getActivationPriority(ind);
		MultiString multi = new MultiString(ms);
		if (multi.isBlank())
			return dms.createMsgBlank(ap);
		else {
			return dms.createMsg(ms, false, ap, OPERATOR, lcs,
				user.getName(), null);
		}
	}

	/** Create a MULTI string for a lane use indication */
	protected String createIndicationMulti(int ind, int w, int h) {
		String m = "";
		LaneUseMulti lum = LaneUseMultiHelper.find(ind, w, h);
		if(lum != null) {
			QuickMessage qm = lum.getQuickMessage();
			if(qm != null)
				m = qm.getMulti();
		}
		if(m.length() > 0 ||
		   LaneUseIndication.fromOrdinal(ind) == LaneUseIndication.DARK)
			return m;
		else
			return null;
	}

	/** Phase to send all sign messages to DMS */
	protected class SendMessages extends Phase {

		/** Send sign messages */
		protected Phase poll(CommMessage mess) {
			for(int lane = 0; lane < msgs.length; lane++)
				sendIndication(lane);
			return null;
		}
	}

	/** Send an indication to a DMS */
	protected void sendIndication(int lane) {
		DMSImpl dms = dmss[lane];
		if(dms != null) {
			if(dms.shouldActivate(msgs[lane]))
				sendIndication(lane, dms);
		}
	}

	/** Send an indication to a DMS */
	protected void sendIndication(int lane, DMSImpl dms) {
		SignMessage sm = msgs[lane];
		try {
			dms.doSetMessageNext(sm);
			ind_after[lane] = indications[lane];
		}
		catch (TMSException e) {
			logError("OpSendLCSIndications.sendIndication: " +
				dms.getName() + ", " + e.getMessage());
		}
	}
}
