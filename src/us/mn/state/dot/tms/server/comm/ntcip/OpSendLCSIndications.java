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
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseMultiHelper;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;

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

	/** Sign messages for each DMS in the LCS array */
	protected final SignMessage[] msgs;

	/** User who sent the indications */
	protected final User user;

	/** Create a new operation to send LCS indications */
	public OpSendLCSIndications(LCSArrayImpl l, Integer[] ind, User u) {
		super(DEVICE_DATA, l);
		indications = ind;
		msgs = new SignMessage[ind.length];
		user = u;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new CreateSignMessages();
	}

	/** Phase to create sign messages */
	protected class CreateSignMessages extends Phase {

		/** Lane of DMS for sign message */
		protected int lane = 0;

		/** Create a sign message */
		protected Phase poll(AddressedMessage mess) {
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
		if(dms != null) {
			String ms = createIndicationMulti(ind);
			if(ms != null) {
				// This is a *slow* operation, because it has
				// to schedule a job on the SONAR task processor
				// thread, which might have a queue of tasks
				// already pending.
				return dms.createMessage(ms,
					getActivationPriority(ind));
			}
		}
		return null;
	}

	/** Create a MULTI string for a lane use indication */
	protected String createIndicationMulti(int ind) {
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

	/** Phase to send all sign messages to DMS */
	protected class SendMessages extends Phase {

		/** Send sign messages */
		protected Phase poll(AddressedMessage mess) {
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
			dms.doSetMessageNext(sm, user);
			ind_after[lane] = indications[lane];
		}
		catch(TMSException e) {
			System.err.println(
				"OpSendLCSIndications.sendIndication: " +
				dms.getName() + ", " + e.getMessage());
		}
	}
}
