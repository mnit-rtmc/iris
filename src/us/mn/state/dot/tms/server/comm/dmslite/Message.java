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
import java.io.InputStream;
import java.io.OutputStream;
import javax.mail.MessagingException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.utils.Log;
import us.mn.state.dot.tms.utils.SString;
import us.mn.state.dot.tms.utils.STime;
import us.mn.state.dot.tms.utils.SEmail;

/**
 * DMS Lite Message. A Message represents the bytes sent and
 * received from a device. The interface is intended to be
 * flexible enough so that a single class can be used for
 * all message types. The DMSLite Message syntax uses XML.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class Message implements AddressedMessage
{
    	/** Root XML tag name. */
	static final String DMSLITEMSGTAG = "DmsLite";

    	/** Default max wait time for DMS response. */
	static final int DEFAULT_TIMEOUT_DMS_MS = 1000 * 30;

	/** Associated operation. */
	OpDms m_opdms = null;

	/** XML request and response element. */
	XmlReqRes m_xmlreqres;

	/** Name for this message. */
	private String m_name = "DmsLiteMsg";

	/** Default timeout in ms. */
	private int m_dmsTimeoutMS = DEFAULT_TIMEOUT_DMS_MS;

	/** Completion time in ms. */
	private int m_completiontimeMS = 0;

	/** streams */
	private final TokenStreamReader m_is;
	private final OutputStream m_os;

	/** Create a new message */
	public Message(OutputStream os, InputStream is) {
		m_os = os;
		m_is = new TokenStreamReader(is, 
			// buffer size, max cap, sleep time
			1024, 16384, 1000);
	}

	/** toString */
	public String toString() {
		String ret="Message(";
		if (m_xmlreqres!=null) {
			ret+="m_xmlreqres=" + m_xmlreqres.toString();
		}
		ret+=")";
		return ret;
	}

	/** set timeout value in MS */
	public void setTimeoutMS(int ms) {
		m_dmsTimeoutMS=(ms<=0 ? DEFAULT_TIMEOUT_DMS_MS : ms);
		Log.finest("DmsLite.Message.setTimeoutMS("+ms+") called.");
	}

	/** set message name */
	public void setName(String n) {
		m_name = n;
	}

	/** get message name */
	public String getName() {
		return m_name;
	}

	/** set completion time in MS */
	public void setCompletionTimeMS(int ms) {
		m_completiontimeMS=ms;
	}

	/** get completion time in MS */
	public int getCompletionTimeMS() {
		return(m_completiontimeMS);
	}

	/** Add a XmlReqRes to this message. */
	public void add(Object xmlrr) {
		if(!(xmlrr instanceof XmlReqRes))
			throw new IllegalArgumentException(
			    "dmslite.Message.add() wrong arg type.");
		m_xmlreqres = (XmlReqRes)xmlrr; //FIXME: use container?
	}

	/** Update intermediate status */
	private void updateInterStatus(String m) {
		if(m_opdms != null)
			m_opdms.updateInterStatus(m, false);
	}

	/** Send a get request message.
	 * @throws IOException if received response is malformed or timed 
	 *	   out waiting for response. */
	public void getRequest(OpDms op) throws IOException {
		m_opdms = op;
		getRequest();
	}

	/** Send a get request message.
	 * @throws IOException if received response is malformed or timed 
	 *	   out waiting for response. */
	public void getRequest() throws IOException {

		// send request
		long starttime = sendRequest();

		// read response
		readResponse(starttime);
	}

	/** Send a request */
	private long sendRequest() throws IOException {
		// build message
		byte[] array = buildReqMsg();

		// send message
		updateInterStatus("Sending request to sensorserver.");
		long starttime=STime.getCurTimeUTCinMillis();
		Log.finest("getRequest(): Writing " + array.length + 
			" bytes to SensorServer.");
		m_is.resetBuffer();
		m_os.write(array);
		m_os.flush();
		return starttime;
	}

	/** Read a response */
	private void readResponse(long starttime) throws IOException {

		updateInterStatus("Waiting for sensorserver.");
		String token = null;
		try {
			token = m_is.readToken(m_dmsTimeoutMS,
		       	"<" + DMSLITEMSGTAG + ">","</" + DMSLITEMSGTAG + ">");
			setCompletionTimeMS((int)STime.
				calcTimeDeltaMS(starttime));
			Log.finer("Response received in " + 
				getCompletionTimeMS() + " ms.");
		} catch(IllegalStateException ex) {
			String msg = "Contact AHMCT: buffer capacity exceeded.";
			handleAwsFailure(msg);
			throw new IOException(msg);
		} catch(IOException ex) {
			String msg = "Unable to connect to SensorServer.";
			handleAwsFailure(msg);
			Log.warning(msg);
			throw new IOException(msg);
		} catch(Exception ex) {
			String msg = "Unexpected problem: " + ex;
			handleAwsFailure(msg);
			throw new IOException(msg);
		}

		// timed out?
		if(token == null) {
			String err="";
			err+="Warning: dmslite.Message.getRequest(): " +
				"timed out waiting for CMS " + // m_dms + 
				" (" + (getCompletionTimeMS()/1000) + 
				"seconds). Timeout is " + m_dmsTimeoutMS 
				/ 1000 + " secs). ";
			handleAwsFailure(err);
			Log.severe(err);
			throw new IOException(err);

		// parse response
		} else {
			Log.finest("dmslite.Message.getRequest(): found "+
				"complete token:" + token);
			// throws IOException
			m_xmlreqres.parseResponse(DMSLITEMSGTAG, token);
		}
	}

	/** Search for a request or response value by name.
	  * @return null if not found else the value. */
	protected String searchForReqResItem(String name) {
		if (m_xmlreqres==null)
			return null;
		return m_xmlreqres.getResponseValue(name);
	}

	/** Return true if message is owned by the AWS */
	protected boolean ownerIsAws() {
		return OpDms.ownerIsAws(searchForReqResItem("Owner"));
	}

	/** 
	  * Determine if failure sending an AWS message to the SensorServer
	  * occurred. 
	  * @return true on failure else false.
	  */
	protected boolean checkAwsFailure() {
		Log.finest("Message.checkAwsFailure() called. this=" + 
			toString() + ", ownerIsAws=" + ownerIsAws());
 		if(m_xmlreqres == null)
			return false;
		String ret=null;

		// IsValid: was there an error?
		String isvalid=this.searchForReqResItem("IsValid");
		if(isvalid == null || isvalid.toLowerCase().equals("true"))
			return false;

		// owner isn't aws?
		if(!ownerIsAws())
			return false;

		// at this point we know there was an error to report
		return true;
	}

	/** Generate an aws failure message */
	protected String getAwsFailureMessage() {
		if(m_xmlreqres == null)
			return "";

		String ret = "";

		// Owner: was owner the aws?
		String owner = this.searchForReqResItem("Owner");
		if(owner == null)
			owner = "";

		// ErrMsg: get the error description
		String errmsg = this.searchForReqResItem("ErrMsg");
		if(errmsg == null)
			errmsg = "";

		// Id: get message id
		String id = this.searchForReqResItem("Id");
		if(id == null)
			id = "";

		// Address: get cms number
		String address = this.searchForReqResItem("Address");
		if(address == null)
			address = "";

		// MsgText: actual message
		String msg = this.searchForReqResItem("MsgText");
		if(msg == null)
			msg = "";

		// build error string
		StringBuilder b = new StringBuilder();
		b.append("Could not send an AWS message to a DMS: reason=");
		b.append(errmsg);
		b.append(", DMS=");
		b.append(address);
		b.append(", message id=");
		b.append(id);
		b.append(", time=");
		b.append(STime.getCurDateTimeString(true));
		b.append(", message=");
		b.append(msg);
		b.append(", author=");
		b.append(owner);
		b.append(", note=");	// appended to by handleAwsFailure()
		return ret;
	}

	/** This method handles a failure when IRIS fails to send an AWS 
	 *  message to a DMS.
	 *  @param errmsgnote Optional error message, appended to generated 
	 *	  message. */
	public void handleAwsFailure(String errmsgnote) {
		if(errmsgnote == null)
			errmsgnote = "";

		// owner is not aws?
		if(!ownerIsAws())
			return;

		// generate an error message
		String errmsg = getAwsFailureMessage() + errmsgnote;
		Log.warning("Warning: failure to send AWS message to DMS: " + 
			errmsg);

		// build email
		String sender = SystemAttrEnum.EMAIL_SENDER_SERVER.getString();
		String recipient=SystemAttrEnum.EMAIL_RECIPIENT_AWS.getString();
		String subject = "IRIS could not send AWS message to DMS";

		// send
		if(recipient == null || sender == null ||
			recipient.length() <= 0 || sender.length() <= 0)
		{
			Log.warning("Message.handleAwsFailure(): didn't try "+
				"to send AWS error email.");
		} else {
			SEmail email = new SEmail(sender, recipient, subject,
				errmsg);
			try {
				email.send();
				Log.finest("Message.handleAwsFailure(): " + 
					"sent email");
			}
			catch(MessagingException e) {
				Log.warning("Message.handleAwsFailure(): " +
					"email failed: " + e.getMessage());
			}
		}
	}

	/** Send an set request message */
	public void setRequest() throws IOException {}

	/** Return a request message with this format:
	 *     <DmsLite><msg name>...etc...</msg name></DmsLite> */
	public byte[] buildReqMsg() {
		if(m_xmlreqres == null)
			return new byte[0];
		return m_xmlreqres.buildReqMsg(DMSLITEMSGTAG);
	}
}
