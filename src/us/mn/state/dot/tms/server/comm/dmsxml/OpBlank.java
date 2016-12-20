/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import static us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller.LOG;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to blank the DMS.
 *
 * @author Michael Darter
 * @author Douglas Lau
 * @author Travis Swanston
 */
class OpBlank extends OpDms
{
	/** blank message, which contains owner, duration, priority */
	private final SignMessage m_sm;

	/** Create a new DMS query configuration object */
	OpBlank(DMSImpl d, SignMessage mess, User u) {
		super(PriorityLevel.DOWNLOAD, d, "Blanking the CMS", u);
		m_sm = mess;
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		m_dms.setMsgNext(m_sm);
		if (dmsConfigured())
			return new PhaseSetBlank();

		// dms not configured
		Phase phase2 = new PhaseSetBlank();
		Phase phase1 = new PhaseGetConfig(phase2);
		return phase1;
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
		xrr.addReq("ActPriority", 
			m_sm.getActivationPriority());
		xrr.addReq("RunPriority", 
			m_sm.getRunTimePriority());
		xrr.addReq("Owner", (m_user != null) ? m_user.getName() : "");

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
	private boolean parseResponse(Message mess, XmlElem xrr) {
		long id = 0;
		boolean valid = false;
		String errmsg = "";

		try {
			// id
			id = xrr.getResLong("Id");

			// isvalid
			valid = xrr.getResBoolean("IsValid");

			// error message text
			errmsg = xrr.getResString("ErrMsg");
			if(!valid && errmsg.length() <= 0)
				errmsg = FAILURE_UNKNOWN;

			// valid resp received?
			LOG.log("OpBlank: isvalid =" + valid);
		} catch (IllegalArgumentException ex) {
			LOG.log("Malformed XML received in OpBlank(msg):" +
				ex + ",id=" + id);
			valid = false;
			errmsg = ex.getMessage();
			handleCommError(EventType.PARSING_ERROR,errmsg);
		}

		// update 
		complete(mess);

		// update dms
		updateMaintStatus("");
		if (valid)
			m_dms.setMsgCurrentNotify(m_sm);
		else {
			LOG.log(
				"OpBlank: response from SensorServer " +
				"received, ignored because Xml valid " +
				"field is false, errmsg=" + errmsg);
			setErrorStatus(errmsg);

			// try again
			if(flagFailureShouldRetry(errmsg)) {
				LOG.log("OpBlank: will retry failed op.");
				return true;

			// give up
			} else {
				// if aws failure, handle it
				if(mess.checkAwsFailure())
					mess.handleAwsFailure(
						"was blanking a msg.");						
			}
		}

		// done
		return false;
	}

	/**
	 * Phase to query the dms config
	 * Note, the type of exception throw here determines
	 * if the messenger reopens the connection on failure.
	 *
	 * @see CommThread#doPoll()
	 * @see Messenger#handleCommError()
	 * @see Messenger#shouldReopen()
	 */
	private class PhaseSetBlank extends Phase
	{
		/** Query the number of modules */
		protected Phase poll(CommMessage argmess)
			throws IOException 
		{
			if(m_sm == null)
				return null;
			assert argmess instanceof Message :
			       "wrong message type";
			Message mess = (Message) argmess;

			// set message attributes as a function of the op
			setMsgAttributes(mess);

			// build xml request and expected response			
			mess.setName(getOpName());
			XmlElem xrr = buildReqRes("SetBlankMsgReqMsg", 
				"SetBlankMsgRespMsg");

			// send request and read response
			mess.add(xrr);
			sendRead(mess);

			if(parseResponse(mess, xrr))
				return this;
			return null;
		}
	}

	/** Cleanup the operation. Called by CommThread.doPoll(). */
	@Override
	public void cleanup() {
		m_dms.setMsgNext(null);
		super.cleanup();
	}
}
