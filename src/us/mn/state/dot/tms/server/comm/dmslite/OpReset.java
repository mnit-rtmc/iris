/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dmslite;

import java.io.IOException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.SString;

/**
 * Operation to reset the DMS.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class OpReset extends OpDms
{
	/** Create a new DMS query status object.
	 *  @param d Current DMS.
	 *  @param u User performing the action, may be null. */
	public OpReset(DMSImpl d, User u) {
		super(DEVICE_DATA, d, "Reinitializing the CMS", u);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		if(dmsConfigured())
			return new PhaseResetDms();

		// dms not configured
		Phase phase2 = new PhaseResetDms();
		Phase phase1 = new PhaseGetConfig(phase2);
		return phase1;
	}

	/** Build request message in this format:
	 *	<DmsLite><SetBlankMsgReqMsg>
	 *		<Id></Id>
	 *		<Address>1</Address>
	 *		<ActPriority>...</ActPriority>
	 *		<RunPriority>...</RunPriority>
	 *		<Owner>...</Owner>
	 *	</SetBlankMsgReqMsg></DmsLite> */
	private XmlElem buildReqRes(String elemReqName, String elemResName) {
		XmlElem xrr = new XmlElem(elemReqName, elemResName);

		// request
		xrr.addReq("Id", generateId());
		xrr.addReq("Address", controller.getDrop());
		xrr.addReq("ActPriority", 
			DMSMessagePriority.OVERRIDE.ordinal());
		xrr.addReq("RunPriority", 
			DMSMessagePriority.BLANK.ordinal());
		xrr.addReq("Owner", 
			m_user != null ? m_user.getName() : "");

		// responses
		xrr.addRes("Id");
		xrr.addRes("IsValid");
		xrr.addRes("ErrMsg");

		return xrr;
	}

	/** Parse response.
	 *	<DmsLite><SetInitRespMsg>
	 *		<Id></Id>
	 *		<IsValid>true</IsValid>
	 *		<ErrMsg></ErrMsg>
	 *	</SetInitRespMsg></DmsLite>
	 *  @return True to retry the operation else false if done. */
	private boolean parseResponse(Message mess, XmlElem xrr) {
		long id = 0;
		boolean valid = false;
		String errmsg = "";

		// parse response
		try {
			// id
			id = xrr.getResLong("Id");

			// valid flag
			valid = xrr.getResBoolean("IsValid");

			// error message text
			errmsg = xrr.getResString("ErrMsg");
			if(!valid && errmsg.length() <= 0)
				errmsg = FAILURE_UNKNOWN;

		} catch (IllegalArgumentException ex) {
			Log.severe("OpReset.PhaseResetDms: " +
				"Malformed XML received:" + ex +
				", id=" + id);
			valid=false;
			errmsg=ex.getMessage();
			handleCommError(EventType.PARSING_ERROR,errmsg);
		}

		// update 
		complete(mess);

		// process response
		if(valid) {
			setErrorStatus("");

			// set blank message
			SignMessage sm = m_dms.createBlankMessage();
			if(sm != null)
                		m_dms.setMessageCurrent(sm, null);

		// valid flag is false
		} else {
			Log.finest("OpReset: isvalid is false, " +
				"errmsg=" + errmsg);
			setErrorStatus(errmsg);

			// try again
			if (flagFailureShouldRetry(errmsg)) {
				Log.finest("OpReset: will retry " +
					"failed operation.");
				return true;
			}
		}
		return false;
	}

	/**
	 * Phase to reset dms.
	 * Note, the type of exception thrown here determines
	 * if the messenger reopens the connection on failure.
	 *
	 * @see MessagePoller#doPoll()
	 * @see Messenger#handleCommError()
	 * @see Messenger#shouldReopen()
	 */
	protected class PhaseResetDms extends Phase
	{
		/** Query current message */
		protected Phase poll(CommMessage argmess) 
			throws IOException 
		{
			updateInterStatus("Starting operation", false);

			Log.finest(
			    "OpReset.PhaseResetDms.poll(msg) called.");

			Message mess = (Message) argmess;

			// set message attributes as a function of the op
			setMsgAttributes(mess);

			// build xml request and expected response			
			mess.setName(getOpName());
			XmlElem xrr = buildReqRes("SetInitReqMsg", 
				"SetInitRespMsg");

			// send request and read response
			mess.add(xrr);
			sendRead(mess);

			if(parseResponse(mess, xrr))
				return this;
			return null;
		}
	}
}
