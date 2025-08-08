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

import us.mn.state.dot.tms.DmsLock;
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

	/** Create a new operation to send LCS indications */
	public OpSendLCSIndications(LcsImpl l, String lk) {
		super(PriorityLevel.COMMAND, l);
		lock = new LcsLock(lk);
		int[] ind = lock.optIndications();
		indications = (ind != null)
			? ind
			: LcsHelper.makeIndications(l, LcsIndication.DARK);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return checkSignsValid() ? new SendMessages() : null;
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
		DmsLock lk = makeDmsLock(ln);
		DMSImpl dms = dmss[ln];
		dms.setLockNotify((lk != null) ? lk.toString() : null, true);
		ind_after[ln] = indications[ln];
	}

	/** Make a DMS lock for an indication */
	private DmsLock makeDmsLock(int ln) {
		int ind = indications[ln];
		String multi = findIndicationMulti(ln, ind);
		if (multi != null) {
			DmsLock lk = new DmsLock(lock.toString());
			lk.setMulti(multi);
			return lk;
		} else
			return null;
	}

	/** Find MULTI string for an LCS indication */
	private String findIndicationMulti(int ln, int ind) {
		LcsState ls = LcsHelper.lookupState(lcs, ln, ind);
		if (ls != null) {
			MsgPattern pat = ls.getMsgPattern();
			if (pat != null) {
				MultiString multi =
					new MultiString(pat.getMulti());
				if (!multi.isBlank())
					return multi.toString();
			}
		}
		return null;
	}
}
