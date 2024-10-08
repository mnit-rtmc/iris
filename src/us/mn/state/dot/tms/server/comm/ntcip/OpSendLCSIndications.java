/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseMultiHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SignMsgPriority;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.User;
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

	/** Indications to send */
	private final Integer[] indications;

	/** Sign messages for each DMS in the LCS array */
	private final SignMessage[] msgs;

	/** Create a new operation to send LCS indications */
	public OpSendLCSIndications(LCSArrayImpl l, Integer[] ind, User u) {
		super(PriorityLevel.COMMAND, l, u);
		assert u != null;
		indications = ind;
		msgs = new SignMessage[ind.length];
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new CreateSignMessages();
	}

	/** Phase to create sign messages */
	protected class CreateSignMessages extends Phase {

		/** Lane of DMS for sign message */
		private int lane = 0;

		/** Create a sign message */
		protected Phase poll(CommMessage mess) {
			if (lane < msgs.length) {
				msgs[lane] = createSignMessage(lane);
				lane++;
				return this;
			} else
				return new SendMessages();
		}
	}

	/** Create a sign message */
	private SignMessage createSignMessage(int lane) {
		int ind = indications[lane];
		DMSImpl dms = dmss[lane];
		if (dms != null) {
			String ms = createIndicationMulti(ind, dms);
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
		MultiString multi = new MultiString(ms);
		if (multi.isBlank())
			return dms.createMsgBlank(SignMsgSource.lcs.bit());
		else {
			String owner = SignMessageHelper.makeMsgOwner(
				SignMsgSource.lcs.bit(),
				user.getName()
			);
			SignMsgPriority mp = SignMsgPriority.high_1;
			return dms.createMsg(ms, owner, false, false, mp, null);
		}
	}

	/** Create a MULTI string for a lane use indication */
	private String createIndicationMulti(int ind, DMSImpl dms) {
		String m = "";
		LaneUseMulti lum = LaneUseMultiHelper.find(ind, dms);
		if (lum != null) {
			MsgPattern pat = lum.getMsgPattern();
			if (pat != null)
				m = pat.getMulti();
		}
		if (m.length() > 0 || LaneUseIndication.DARK.ordinal() == ind)
			return m;
		else
			return null;
	}

	/** Phase to send all sign messages to DMS */
	protected class SendMessages extends Phase {

		/** Send sign messages */
		protected Phase poll(CommMessage mess) {
			for (int lane = 0; lane < msgs.length; lane++)
				sendIndication(lane);
			return null;
		}
	}

	/** Send an indication to a DMS */
	private void sendIndication(int lane) {
		DMSImpl dms = dmss[lane];
		if (dms != null)
			sendIndication(lane, dms);
	}

	/** Send an indication to a DMS */
	private void sendIndication(int lane, DMSImpl dms) {
		SignMessage sm = msgs[lane];
		if (sm != null)
			sendIndication(lane, dms, sm);
		else
			logError("sendIndication: no indication, lane " + lane);
	}

	/** Send an indication to a DMS */
	private void sendIndication(int lane, DMSImpl dms, SignMessage sm) {
		try {
			dms.doSetMsgUser(sm);
			ind_after[lane] = indications[lane];
		}
		catch (TMSException e) {
			logError("sendIndication: " + e.getMessage());
		}
	}
}
