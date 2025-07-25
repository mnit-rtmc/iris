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

import us.mn.state.dot.tms.LcsHelper;
import us.mn.state.dot.tms.LcsIndication;
import us.mn.state.dot.tms.LcsLock;
import us.mn.state.dot.tms.LcsState;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SignMsgPriority;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LcsImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Operation to send new indicaitons to a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpSendLCSIndications extends OpLCS {

	/** LCS lock */
	private final LcsLock lock;

	/** LCS indications to send */
	private final int[] indications;

	/** Sign messages for each DMS in the LCS array */
	private final SignMessage[] msgs;

	/** Create a new operation to send LCS indications */
	public OpSendLCSIndications(LcsImpl l, String lk) {
		super(PriorityLevel.COMMAND, l);
		lock = new LcsLock(lk);
		int[] ind = lock.optIndications();
		indications = (ind != null)
			? ind
			: LcsHelper.makeIndications(l, LcsIndication.DARK);
		msgs = new SignMessage[dmss.length];
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return (dmss.length > 0 && dmss.length == indications.length)
		      ? new CreateSignMessages()
		      : null;
	}

	/** Phase to create sign messages */
	private class CreateSignMessages extends Phase {

		/** Lane of DMS for sign message */
		private int lane = 0;

		/** Create a sign message */
		protected Phase poll(CommMessage mess) {
			msgs[lane] = createSignMessage(lane);
			if (msgs[lane] == null) {
				logError("createSignMessage: " + lane);
				return null;
			}
			lane++;
			return (lane < msgs.length) ? this : new SendMessages();
		}
	}

	/** Create a sign message */
	private SignMessage createSignMessage(int ln) {
		int ind = indications[ln];
		String ms = createIndicationMulti(ln, ind);
		return (ms != null) ? createSignMessage(ln, ms) : null;
	}

	/** Create a MULTI string for an LCS indication */
	private String createIndicationMulti(int ln, int ind) {
		LcsState ls = LcsHelper.lookupState(lcs, ln, ind);
		if (ls != null) {
			MsgPattern pat = ls.getMsgPattern();
			if (pat != null)
				return pat.getMulti();
		}
		return (LcsIndication.DARK.ordinal() == ind) ? "" : null;
	}

	/** Create a sign message.
	 * This is a *slow* operation, because it has to schedule a job on the
	 * SONAR task processor thread, which might have a queue of tasks
	 * already pending. */
	private SignMessage createSignMessage(int ln, String ms) {
		DMSImpl dms = dmss[ln];
		MultiString multi = new MultiString(ms);
		if (multi.isBlank())
			return dms.createMsgBlank(SignMsgSource.lcs.bit());
		else {
			String owner = SignMessageHelper.makeMsgOwner(
				SignMsgSource.lcs.bit(),
				lock.getUser()
			);
			SignMsgPriority mp = SignMsgPriority.high_1;
			return dms.createMsg(ms, owner, false, false, false, mp,
				null);
		}
	}

	/** Phase to send all sign messages to DMS */
	private class SendMessages extends Phase {

		/** Send sign messages */
		protected Phase poll(CommMessage mess) {
			for (int ln = 0; ln < dmss.length; ln++)
				sendIndication(ln);
			return null;
		}
	}

	/** Send an indication to a DMS */
	private void sendIndication(int ln) {
		DMSImpl dms = dmss[ln];
		SignMessage sm = msgs[ln];
		if (sm != null)
			sendIndication(ln, dms, sm);
		else
			logError("sendIndication: no indication, lane " + ln);
	}

	/** Send an indication to a DMS */
	private void sendIndication(int ln, DMSImpl dms, SignMessage sm) {
		try {
			dms.doSetMsgUser(sm);
			ind_after[ln] = indications[ln];
		}
		catch (TMSException e) {
			logError("sendIndication: " + e.getMessage());
		}
	}
}
