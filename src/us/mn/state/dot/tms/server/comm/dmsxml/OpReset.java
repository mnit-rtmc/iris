/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2024  Minnesota Department of Transportation
 * Copyright (C) 2008-2014  AHMCT, University of California
 * Copyright (C) 2012 Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.dmsxml;

import java.io.IOException;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SignMsgPriority;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import static us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller.LOG;

/**
 * Operation to reset the DMS.
 *
 * @author Michael Darter
 * @author Douglas Lau
 * @author Travis Swanston
 */
class OpReset extends OpDms {

	/** Create a new DMS reset object.
	 *  @param d Current DMS. */
	OpReset(DMSImpl d) {
		super(PriorityLevel.SETTINGS, d, "Reinitializing the CMS");
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		if (dmsConfigured())
			return new PhaseResetDms();
		else {
			Phase phase2 = new PhaseResetDms();
			return new PhaseGetConfig(phase2);
		}
	}

	/** Build request message in this format:
	 *	<DmsXml><SetBlankMsgReqMsg>
	 *		<Id></Id>
	 *		<Address>1</Address>
	 *		<ActPriority>...</ActPriority>
	 *		<RunPriority>...</RunPriority>
	 *		<Owner>...</Owner>
	 *	</SetBlankMsgReqMsg></DmsXml> */
	private XmlElem buildReqRes(String elemReqName, String elemResName) {
		XmlElem xrr = new XmlElem(elemReqName, elemResName);

		// request
		xrr.addReq("Id", generateId());
		xrr.addReq("Address", controller.getDrop());
		xrr.addReq("ActPriority", SignMsgPriority.high_sys.ordinal());
		xrr.addReq("RunPriority", SignMsgPriority.low_1.ordinal());
		xrr.addReq("Owner", SignMessageHelper.makeMsgOwner(
			SignMsgSource.reset.bit()));

		// responses
		xrr.addRes("Id");
		xrr.addRes("IsValid");
		xrr.addRes("ErrMsg");

		return xrr;
	}

	/** Parse response.
	 *	<DmsXml><SetInitRespMsg>
	 *		<Id></Id>
	 *		<IsValid>true</IsValid>
	 *		<ErrMsg></ErrMsg>
	 *	</SetInitRespMsg></DmsXml>
	 *  @return True to retry the operation else false if done. */
	private boolean parseResponse(Message mess, XmlElem xrr)
		throws IOException
	{
		long id = 0;
		try {
			id = xrr.getResLong("Id");
			boolean valid = xrr.getResBoolean("IsValid");
			String errmsg = xrr.getResString("ErrMsg");
			if (!valid) {
				LOG.log("OpReset: isvalid is false, " +
					"errmsg=" + errmsg);
				throw new ControllerException(errmsg);
			}
		}
		catch (IllegalArgumentException ex) {
			LOG.log("SEVERE OpReset.PhaseResetDms: " +
				"Malformed XML received:" + ex +
				", id=" + id);
			throw new ParsingException(ex);
		}
		putCtrlFaults(null, null);
		SignMessage sm = m_dms.createMsgBlank(
			SignMsgSource.reset.bit());
		if (sm != null)
			m_dms.setMsgCurrentNotify(sm);
		return false;
	}

	/**
	 * Phase to reset dms.
	 * Note, the type of exception thrown here determines
	 * if the messenger reopens the connection on failure.
	 *
	 * @see CommThread#doPoll()
	 */
	private class PhaseResetDms extends Phase {

		/** Query current message */
		protected Phase poll(CommMessage argmess) throws IOException {
			LOG.log("OpReset.PhaseResetDms.poll(msg) called.");
			Message mess = (Message) argmess;
			setMsgAttributes(mess);
			mess.setName(getOpName());
			XmlElem xrr = buildReqRes("SetInitReqMsg",
				"SetInitRespMsg");
			mess.add(xrr);
			sendRead(mess);
			return parseResponse(mess, xrr) ? this : null;
		}
	}
}
