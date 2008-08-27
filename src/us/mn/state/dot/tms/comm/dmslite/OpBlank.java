/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.comm.AddressedMessage;

import java.io.IOException;

/**
 * Operation to blank the DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpBlank extends OpDms
{
	protected static final String OPNAME = "OpBlank";

	/** associated DMS */
	private final DMSImpl m_dms;

	/** blank message, which contains owner, duration */
	private final SignMessage m_mess;

	/** Create a new DMS query configuration object */
	public OpBlank(DMSImpl d, SignMessage mess) {
		super(DOWNLOAD, d);
		m_dms = d;
		m_mess = mess;
	}

	/** return description of operation, which is displayed in the client */
	public String getOperationDescription() {
		return "Blanking the sign";
	}

	/** Create the first phase of the operation */
	protected Phase phaseOne() {
		return new PhaseSetBlank();
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(success) {
			m_dms.notifyUpdate();
		}

		super.cleanup();
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

			mess.setName(reqname);
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
			ReqRes rr2 = new ReqRes("Owner", m_mess.getOwner(),
						new String[0]);
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
				if(!valid && (errmsg.length() < 1))
					errmsg = "request failed";

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
				m_dms.setActiveMessage(m_mess);
			} else {
				System.err.println(
				    "OpBlank: response from cmsserver received, ignored because Xml valid field is false, errmsg="
				    + errmsg);
				m_dms.setStatus(OPNAME + ": " + errmsg);

				// try again
				if(flagFailureShouldRetry(errmsg)) {
					System.err.println(
					    "OpBlank: will retry failed operation");
					return this;

				// give up
				} else {
					// if caws failure, handle it
					if( mess.checkCAWSFailure() )
						mess.handleCAWSFailure("was blanking a message.");						
				}
			}

			// done
			return null;
		}
	}
}
