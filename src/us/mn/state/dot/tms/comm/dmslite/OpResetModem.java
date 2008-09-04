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

import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.ntcip.DmsMessageMemoryType;
import us.mn.state.dot.tms.comm.ntcip.DmsMessageMultiString;
import us.mn.state.dot.tms.comm.ntcip.DmsMessageStatus;
import us.mn.state.dot.tms.comm.ntcip.DmsMessageTimeRemaining;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.TMSObjectImpl;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TreeMap;

/**
 * Operation to reset the DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpResetModem extends OpDms
{
	protected static final String OPNAME="OpResetModem";

	/** Create a new DMS query status object */
	public OpResetModem(DMSImpl d) {
		super(DEVICE_DATA, d);
	}

	/** return description of operation, which is displayed in the client */
	public String getOperationDescription() {
		return "Reinitializing the CMS Modem";
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		System.err.println("dmslite.OpResetModem.phaseOne() called.");
		return new PhaseResetDms();
	}

	/**
	 * Phase to reset dms.
	 * Note, the type of exception thrown here determines
	 * if the messenger reopens the connection on failure.
	 *
	 * @see MessagePoller#doPoll()
	 * @see Messenger#handleException()
	 * @see Messenger#shouldReopen()
	 */
	protected class PhaseResetDms extends Phase
	{
		/** Query current message */
		protected Phase poll(AddressedMessage argmess) throws IOException {
			System.err.println(
			    "OpResetModem.PhaseResetDms.poll(msg) called.");
			assert argmess instanceof Message : "wrong message type";

			Message mess = (Message) argmess;

			// set message attributes as a function of the operation
			setMsgAttributes(mess);

			// build req msg and expected response
			mess.setName(OPNAME);
			mess.setReqMsgName("SetInitModemReqMsg");
			mess.setRespMsgName("SetInitModemRespMsg");
			// FIXME: fields below are placeholders and non-functional until this operation is implemented
			// id
			ReqRes rr0 = new ReqRes("Id", generateId(), new String[] {"Id"});
			mess.add(rr0);

			// everything else
			String addr = Integer.toString(controller.getDrop());
			ReqRes rr1 = new ReqRes("Address", addr, new String[] {"IsValid", "ErrMsg"});
			mess.add(rr1);

			// send msg
			mess.getRequest(); // throws IOException

			// parse resp msg
			// e.g. <DmsLite><SetInitRespMsg><Id>123</Id><IsValid>true</IsValid><ErrMsg></ErrMsg></SetInitRespMsg></DmsLite>
			long id = 0;
			boolean valid = false;
			String errmsg = "";

			// parse response
			try {
				// id
				id = new Long(rr0.getResVal("Id"));

				// valid flag
				valid = new Boolean(rr1.getResVal("IsValid"));

				// error message text
				errmsg = rr1.getResVal("ErrMsg");
				if (!valid && errmsg.length()<1)
					errmsg="request failed";

			} catch (IllegalArgumentException ex) {
				System.err.println(
				    "OpResetModem.PhaseResetDms: Malformed XML received:"+ ex+", id="+id);
				valid=false;
				errmsg=ex.getMessage();
				handleException(new IOException(errmsg));
			}

			// update 
			complete(mess);

			// process response
			if(valid) {

				// set blank message
				String owner="";	//FIXME: how to get owner name? TMSObjectImpl.getUserName();
				SignMessage sm=OpDms.createBlankMsg(m_dms,owner);
                		m_dms.setActiveMessage(sm);

			// valid flag is false
			} else {
				System.err.println(
				    "OpResetModem: response from cmsserver received, ignored because Xml valid field is false, errmsg="+errmsg);
				m_dms.setStatus(OPNAME+": "+errmsg);

				// try again
				if (flagFailureShouldRetry(errmsg)) {
					System.err.println("OpResetModem: will retry failed operation.");
					return this;
				}
			}

			// this operation is complete
			return null;
		}
	}
}

