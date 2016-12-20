/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
 * Copyright (C) 2008-2010  AHMCT, University of California
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
import java.io.InputStream;
import java.io.OutputStream;
import javax.mail.MessagingException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import static us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller.LOG;
import us.mn.state.dot.tms.utils.Emailer;
import us.mn.state.dot.tms.utils.SString;

/**
 * DMS XML message. A Message represents the bytes sent and
 * received from a device. The interface is intended to be
 * flexible enough so that a single class can be used for
 * all message types. The DmsXml Message syntax uses XML.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
class Message implements CommMessage
{
    	/** Root XML tag name. */
	public final static String DMSXMLMSGTAG = "DmsXml";

	/** Intermediate status update XML tag name */
	public final static String ISTATUSTAG = "InterStatus";

    	/** Default max wait time for DMS response. */
	public final static int DEFAULT_TIMEOUT_DMS_MS = 1000 * 30;

	/** Associated operation. */
	private OpDms m_opdms = null;

	/** Container of XML elements. */
	XmlElems m_xelems = new XmlElems();

	/** Name for this message. */
	private String m_name = "DmsXmlMsg";

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
		String ret = "Message(";
		if(m_xelems != null)
			ret += "m_xelems=" + m_xelems.toString();
		ret += ")";
		return ret;
	}

	/** set timeout value in MS */
	public void setTimeoutMS(int ms) {
		m_dmsTimeoutMS = (ms <= 0 ? DEFAULT_TIMEOUT_DMS_MS : ms);
		LOG.log("DmsXml.Message.setTimeoutMS(" + ms + 
			") called.");
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
		m_completiontimeMS = ms;
	}

	/** get completion time in MS */
	public int getCompletionTimeMS() {
		return(m_completiontimeMS);
	}

	/** Log a property query */
	@Override
	public void logQuery(ControllerProperty prop) {
		// not implemented
	}

	/** Log a property store */
	@Override
	public void logStore(ControllerProperty prop) {
		// not implemented
	}

	/** Log an error */
	@Override
	public void logError(String m) {
		// not implemented
	}

	/** Add an XmlElem to this message */
	public void add(ControllerProperty xmlrr) {
		if(!(xmlrr instanceof XmlElem))
			throw new IllegalArgumentException(
			    "dmsxml.Message.add() wrong arg type.");
		m_xelems.add((XmlElem)xmlrr);
	}

	/** Set the associated operation */
	public void setOperation(OpDms op) {
		m_opdms = op;
	}

	/** Query the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void queryProps() throws IOException {

		// send request
		long starttime = sendRequest();

		// read XML elements in response
		readElements(starttime);
	}

	/** Send a request */
	private long sendRequest() throws IOException {
		// build message
		byte[] array = buildReqMsg();

		// send message
		long starttime=TimeSteward.currentTimeMillis();
		LOG.log("queryProps(): Writing " + array.length + 
			" bytes to SensorServer: " + 
			SString.byteArrayToString(array) + ".");
		m_is.resetBuffer();
		m_os.write(array);
		m_os.flush();
		return starttime;
	}

	/** Get DMS id */
	private String getDmsId() {
		if(m_opdms != null)
			return m_opdms.m_dms.getName();
		return "V?";
	}

	/** Read one XML element in the response. */
	private void readElements(long starttime) throws IOException {
		/** The intermediate status is updated in finally block. */
		String[] istatus = new String[0];
		long startms = TimeSteward.currentTimeMillis();
		do {
			String token = null;
			try {
				long elapsed = STime.calcTimeDeltaMS(startms);
				int leftms = (int)(m_dmsTimeoutMS - elapsed);
				if(leftms > 0) {
					token = m_is.readToken(leftms,
					       	"<" + DMSXMLMSGTAG + ">", 
						"</" + DMSXMLMSGTAG + ">");
					setCompletionTimeMS((int)STime.
						calcTimeDeltaMS(starttime));
					LOG.log("Response received in " + 
						getCompletionTimeMS() + 
						" ms.");
				}
			} catch(IllegalStateException ex) {
				istatus = new String[] 
					{"Warning: buffer cap exceeded."};
				handleAwsFailure(istatus[0]);
				throw new IOException(istatus[0]);
			} catch(IOException ex) {
				istatus = new String[] 
					{"Can't connect to SensorServer."};
				handleAwsFailure(istatus[0]);
				LOG.log("WARNING: " + istatus[0]);
				throw new IOException(istatus[0]);
			} catch(Exception ex) {
				istatus = new String[] 
					{"Unexpected problem: " + ex};
				handleAwsFailure(istatus[0]);
				throw new IOException(istatus[0]);
			}

			// timed out?
			if(token == null) {
				String dmsid = getDmsId();
				String err = "";
				err += "dmsxml.Message.readElements(): " +
					"timed out waiting for " + dmsid + "("
					+ (getCompletionTimeMS() / 1000) + 
					" seconds). Timeout is " + 
					m_dmsTimeoutMS / 1000 + " secs). ";
				handleAwsFailure(err);
				LOG.log(err);
				istatus = new String[] {"Timed out waiting " +
					"for sensorserver."};
				throw new IOException("Timed out waiting " +
					"for " + dmsid);
			}

			// parse response
			LOG.log("dmsxml.Message.queryProps(): " +
				"found complete token:" + token);

			// can throw IOException
			istatus = new String[] {"Parse error"};
			// sets 'was read' flag for each XML element
			m_xelems.parseResponse(Message.DMSXMLMSGTAG,
				Message.ISTATUSTAG, token);

			// Either a completed response element or an
			// intermediate status update was read.
			istatus = getInterStatusMsgs();
		} while(!m_xelems.readDone());
	}

	/** Get the response value by name.
	  * @return null if not found else the value. */
	protected String getResString(String name) {
		if(m_xelems == null)
			return null;
		return m_xelems.getResString(name);
	}

	/** Return true if message is owned by the AWS. */
	protected boolean ownerIsAws() {
		return OpDms.ownerIsAws(getResString("Owner"));
	}

	/** Determine if failure sending an AWS message to the 
	 *  SensorServer occurred. 
	 * @return true on failure else false. */
	protected boolean checkAwsFailure() {
		LOG.log("Message.checkAwsFailure() called. this=" + 
			toString() + ", ownerIsAws=" + ownerIsAws());
 		if(m_xelems == null)
			return false;
		String ret=null;

		// IsValid: was there an error?
		String isvalid = getResString("IsValid");
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
		if(m_xelems == null)
			return "";

		String ret = "";

		// Owner: was owner the aws?
		String owner = getResString("Owner");
		if(owner == null)
			owner = "";

		// ErrMsg: get the error description
		String errmsg = getResString("ErrMsg");
		if(errmsg == null)
			errmsg = "";

		// Id: get message id
		String id = getResString("Id");
		if(id == null)
			id = "";

		// Address: get cms number
		String address = getResString("Address");
		if(address == null)
			address = "";

		// MsgText: actual message
		String msg = getResString("MsgText");
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
		LOG.log("WARNING: failure to send AWS message to DMS: " + 
			errmsg);

		// build email
		String host = SystemAttrEnum.EMAIL_SMTP_HOST.getString();
		String sender = SystemAttrEnum.EMAIL_SENDER_SERVER.getString();
		String recip = SystemAttrEnum.EMAIL_RECIPIENT_AWS.getString();
		String subject = "IRIS could not send AWS message to DMS";

		// send
		if(host == null || host.length() <= 0 ||
		   recip == null || recip.length() <= 0 ||
		   sender == null || sender.length() <= 0)
		{
			LOG.log("WARNING: didn't" +
				"try to send AWS error email.");
		} else {
			try {
				Emailer email = new Emailer(host, sender,recip);
				email.send(subject, errmsg);
				LOG.log("Message.handleAwsFailure(): " + 
					"sent email");
			}
			catch(MessagingException e) {
				LOG.log("WARNING: " +
					"email failed: " + e.getMessage());
			}
		}
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void storeProps() throws IOException {}

	/** Return a request message with this format:
	 *     <DmsXml><msg name>...etc...</msg name></DmsXml> */
	public byte[] buildReqMsg() {
		if(m_xelems == null)
			return new byte[0];
		return m_xelems.buildReqMsg(DMSXMLMSGTAG);
	}

	/** Get pending intermediate status messages */
	public String[] getInterStatusMsgs() {
		return m_xelems.getInterStatusMsgs();
	}
}
