/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.SString;

/**
 * Operation to blank the DMS.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class OpBlank extends OpDms
{
	/** blank message, which contains owner, duration, priority */
	private final SignMessage m_sm;

	/** Create a new DMS query configuration object */
	public OpBlank(DMSImpl d, SignMessage mess, User u) {
		super(DOWNLOAD, d, "Blanking the CMS", u);
		m_sm = mess;
	}

	/** Create the first phase of the operation */
	protected Phase phaseOne() {
		if(dmsConfigured())
			return new PhaseSetBlank();

		// dms not configured
		Phase phase2 = new PhaseSetBlank();
		Phase phase1 = new PhaseGetConfig(phase2);
		return phase1;
	}

	/**
	 * Phase to query the dms config
	 * Note, the type of exception throw here determines
	 * if the messenger reopens the connection on failure.
	 *
	 * @see MessagePoller#doPoll()
	 * @see Messenger#handleCommError()
	 * @see Messenger#shouldReopen()
	 */
	protected class PhaseSetBlank extends Phase
	{
		/** Query the number of modules */
		protected Phase poll(AddressedMessage argmess)
			throws IOException 
		{
			updateInterStatus("Starting operation", false);

			if(m_sm == null)
				return null;
			assert argmess instanceof Message :
			       "wrong message type";
			Message mess = (Message) argmess;

			// set message attributes as a function of the operation
			setMsgAttributes(mess);

			// build message: 
			// <DmsLite><SetBlankMsgReqMsg>
			//	<Id></Id>
			//	<Address>1</Address>
			//	<ActPriority>3</ActPriority>
			//	<RunPriority>3</RunPriority>
			//	<Owner>bob</Owner>
			// </SetBlankMsgReqMsg></DmsLite>

			// build xml request and expected response			
			XmlReqRes xrr = new XmlReqRes("SetBlankMsgReqMsg", 
				"SetBlankMsgRespMsg");
			mess.setName(getOpName());

			// id
			xrr.add(new ReqRes("Id", generateId(),
				new String[] { "Id" }));

			// address
			xrr.add(new ReqRes("Address", controller.getDrop(),
				new String[] { "IsValid", "ErrMsg" }));

			// ActPriority
			xrr.add(new ReqRes("ActPriority", 
				m_sm.getActivationPriority(), new String[0]));

			// RunPriority
			xrr.add(new ReqRes("RunPriority", 
				m_sm.getRunTimePriority(), new String[0]));

			// owner
			xrr.add(new ReqRes("Owner", 
				m_user != null ? m_user.getName() : "", 
				new String[0]));

			// send request and read response
			mess.add(xrr);
			sendRead(mess);

			// response: 
			// <DmsLite><SetBlankMsgRespMsg>
			//	<Id></Id><IsValid>true</IsValid><ErrMsg></ErrMsg>
			// </SetBlankMsgRespMsg></DmsLite>

			// parse resp msg
			long id = 0;
			boolean valid = false;
			String errmsg = "";

			try {
				// id
				id = new Long(xrr.getResValue("Id"));

				// isvalid
				valid = new Boolean(xrr.getResValue("IsValid"));

				// error message text
				errmsg = xrr.getResValue("ErrMsg");
				if(!valid && errmsg.length() <= 0)
					errmsg = FAILURE_UNKNOWN;

				// valid resp received?
				Log.finest("dmslite.OpBlank.PhaseSetBlank.poll(): success="
					+ valid);
			} catch (IllegalArgumentException ex) {
				Log.severe(
					"Malformed XML received in dmslite.OpBlank.PhaseSetBlank.poll(msg):"
					+ ex + ",id=" + id);
				valid = false;
				errmsg = ex.getMessage();
				handleCommError(EventType.PARSING_ERROR,errmsg);
			}

			// update 
			complete(mess);

			// update dms
			if(valid) {
				m_dms.setMessageCurrent(m_sm, m_user);
			} else {
				Log.finest(
					"OpBlank: response from SensorServer " +
					"received, ignored because Xml valid " +
					"field is false, errmsg=" + errmsg);
				errorStatus = errmsg;

				// try again
				if(flagFailureShouldRetry(errmsg)) {
					Log.finest("OpBlank: will retry failed operation");
					return this;

				// give up
				} else {
					// if aws failure, handle it
					if(mess.checkAwsFailure())
						mess.handleAwsFailure("was blanking a message.");						
				}
			}

			// done
			return null;
		}
	}
}
