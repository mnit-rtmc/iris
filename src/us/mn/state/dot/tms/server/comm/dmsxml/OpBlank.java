/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import static us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller.LOG;

/**
 * Operation to blank the DMS.
 *
 * @author Michael Darter
 * @author Douglas Lau
 * @author Travis Swanston
 */
class OpBlank extends OpDms {

	/** blank message, which contains owner, duration, priority */
	private final SignMessage m_sm;

	/** Create a new DMS blank object */
	OpBlank(DMSImpl d, SignMessage mess) {
		super(PriorityLevel.COMMAND, d, "Blanking the CMS");
		m_sm = mess;
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		if (m_sm == null)
			return null;
		m_dms.setMsgNext(m_sm);
		if (dmsConfigured())
			return new PhaseSetBlank();
		else {
			Phase phase2 = new PhaseSetBlank();
			return new PhaseGetConfig(phase2);
		}
	}

	/** Build request message in this format:
	 *	<DmsXml><SetBlankMsgReqMsg>
	 *		<Id></Id>
	 *		<Address>1</Address>
	 *		<ActPriority>3</ActPriority>
	 *		<RunPriority>3</RunPriority>
	 *		<Owner>bob</Owner>
	 *	</SetBlankMsgReqMsg></DmsXml>
	 */
	private XmlElem buildReqRes(String elemReqName, String elemResName) {
		XmlElem xrr = new XmlElem(elemReqName, elemResName);

		// request
		xrr.addReq("Id", generateId());
		xrr.addReq("Address", controller.getDrop());
		xrr.addReq("ActPriority", m_sm.getMsgPriority());
		xrr.addReq("RunPriority", m_sm.getMsgPriority());
		xrr.addReq("Owner", m_sm.getMsgOwner());

		// response
		xrr.addRes("Id");
		xrr.addRes("IsValid");
		xrr.addRes("ErrMsg");

		return xrr;
	}

	/** Parse response.
	 *	<DmsXml><SetBlankMsgRespMsg>
	 *		<Id></Id>
	 *		<IsValid>true</IsValid>
	 *		<ErrMsg></ErrMsg>
	 *	</SetBlankMsgRespMsg></DmsXml>
	 *  @return True to retry the operation else false if done. */
	private boolean parseResponse(Message mess, XmlElem xrr)
		throws IOException
	{
		long id = 0;
		try {
			id = xrr.getResLong("Id");
			String errmsg = xrr.getResString("ErrMsg");
			boolean valid = xrr.getResBoolean("IsValid");
			LOG.log("OpBlank: isvalid =" + valid);
			if (!valid) {
				LOG.log(
					"OpBlank: response ignored because " +
					"valid is false, errmsg=" + errmsg
				);
				throw new ControllerException(errmsg);
			}
		}
		catch (IllegalArgumentException ex) {
			LOG.log("Malformed XML received in OpBlank(msg):" +
				ex + ",id=" + id);
			throw new ParsingException(ex);
		}
		m_dms.setMsgCurrentNotify(m_sm, true);
		return false;
	}

	/**
	 * Phase to query the dms config
	 * Note, the type of exception throw here determines
	 * if the messenger reopens the connection on failure.
	 *
	 * @see CommThread#doPoll()
	 */
	private class PhaseSetBlank extends Phase {

		/** Query the number of modules */
		protected Phase poll(CommMessage argmess) throws IOException {
			assert argmess instanceof Message :
			       "wrong message type";
			Message mess = (Message) argmess;
			setMsgAttributes(mess);
			mess.setName(getOpName());
			XmlElem xrr = buildReqRes("SetBlankMsgReqMsg",
				"SetBlankMsgRespMsg");
			mess.add(xrr);
			sendRead(mess);
			return parseResponse(mess, xrr) ? this : null;
		}
	}

	/** Cleanup the operation. Called by CommThread.doPoll(). */
	@Override
	public void cleanup() {
		m_dms.setMsgNext(null);
		super.cleanup();
	}
}
