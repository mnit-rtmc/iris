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

package us.mn.state.dot.tms.comm.dmslite;

import java.io.IOException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * Operation to blank the DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpBlank extends OpDms
{
	/** blank message, which contains owner, duration */
	private final SignMessage m_mess;

	/** Create a new DMS query configuration object */
	public OpBlank(DMSImpl d, SignMessage mess, User u) {
		super(DOWNLOAD, d, "Blanking the CMS", u);
		m_mess = mess;
	}

	/** Create the first phase of the operation */
	protected Phase phaseOne() {
		return new PhaseSetBlank();
	}

	/**
	 * Phase to query the dms config
	 * Note, the type of exception throw here determines
	 * if the messenger reopens the connection on failure.
	 *
	 * @see MessagePoller#doPoll()
	 * @see Messenger#handleException()
	 * @see Messenger#shouldReopen()
	 */
	protected class PhaseSetBlank extends Phase
	{
		/** Query the number of modules */
		protected Phase poll(AddressedMessage argmess)
			throws IOException {

			System.err.println(
			    "dmslite.OpBlank.PhaseGetConfig.poll(msg) called. m_mess.duration="
			    + m_mess.getDuration());
			assert argmess instanceof Message :
			       "wrong message type";

			Message mess = (Message) argmess;

			// sanity check
			if(m_mess.getDuration() > 0) {
				System.err.println(
				    "Bogus duration received in OpBlank.PhaseSetBlank().");

				return (null);
			}

			// set message attributes as a function of the operation
			setMsgAttributes(mess);

			// build message: <DmsLite><SetBlankMsgReqMsg><Id></Id><Address>1</Address><Owner>bob</Owner></SetBlankMsgReqMsg></DmsLite>

			// build req msg
			String reqname = "SetBlankMsgReqMsg";
			String resname = "SetBlankMsgRespMsg";

			mess.setName(getOpName());
			mess.setReqMsgName(reqname);
			mess.setRespMsgName(resname);

			// id
			ReqRes rr0 = new ReqRes("Id", generateId(),
						new String[] { "Id" });
			mess.add(rr0);

			// drop
			String drop = Integer.toString(controller.getDrop());
			ReqRes rr1 = new ReqRes("Address", drop,
						new String[] { "IsValid",
				"ErrMsg" });
			mess.add(rr1);

			// owner
			String user = m_user != null ? m_user.getName() : "";
			ReqRes rr2 = new ReqRes("Owner", user, new String[0]);
			mess.add(rr2);

			// send msg
			mess.getRequest();    // throws IOException

			// response: <DmsLite><SetBlankMsgRespMsg><Id></Id><IsValid>true</IsValid><ErrMsg></ErrMsg></SetBlankMsgRespMsg></DmsLite>

			// parse resp msg
			long id = 0;
			boolean valid = false;
			String errmsg = "";

			try {
				// id
				id = new Long(rr0.getResVal("Id"));

				// isvalid
				valid = new Boolean(rr1.getResVal("IsValid"));

				// error message text
				errmsg = rr1.getResVal("ErrMsg");
				if(!valid && errmsg.length() <= 0)
					errmsg = FAILURE_UNKNOWN;

				// valid resp received?
				System.err.println(
				    "dmslite.OpBlank.PhaseSetBlank.poll(): success="
				    + valid);
			} catch (IllegalArgumentException ex) {
				System.err.println(
				    "Malformed XML received in dmslite.OpBlank.PhaseSetBlank.poll(msg):"
				    + ex + ",id=" + id);
				valid = false;
				errmsg = ex.getMessage();
				handleException(new IOException(errmsg));
			}

			// update 
			complete(mess);

			// update dms
			if(valid) {
				m_dms.setMessageCurrent(m_mess, m_user);
			} else {
				System.err.println(
				    "OpBlank: response from cmsserver received, ignored because Xml valid field is false, errmsg="
				    + errmsg);
				errorStatus = errmsg;

				// try again
				if(flagFailureShouldRetry(errmsg)) {
					System.err.println(
					    "OpBlank: will retry failed operation");
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
