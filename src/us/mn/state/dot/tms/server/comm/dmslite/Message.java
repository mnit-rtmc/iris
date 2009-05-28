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
import java.util.LinkedList;
import javax.mail.MessagingException;
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
 * @author Douglas Lau
 * @author Michael Darter
 */
public class Message implements AddressedMessage
{
    	// xml tag
	static final String DMSLITEMSGTAG = "DmsLite";

    	// default max wait time for DMS response
	static final int DEFAULT_TIMEOUT_DMS_MS = 1000*30;

	// fields
	private String m_name = "DmsLiteMsg";
	private String m_reqmsgname = "";
	private String m_respmsgname = "";
	private int m_dmsTimeoutMS=DEFAULT_TIMEOUT_DMS_MS;
	private int m_completiontimeMS=0;

	/** List of objects set or get with this message */
	protected final LinkedList<Object> m_objlist = 
		new LinkedList<Object>();
	private final TokenStreamReader m_is;
	private final OutputStream m_os;

	/** Create a new message */
	public Message(OutputStream os, InputStream is) {
		m_os = os;
		m_is = new TokenStreamReader(
		    is, 1024, 16384, 1000);    // buffer size, max cap, sleep time
		//Log.finest("dmslite.Message.Message() called.");
	}

	/** toString */
	public String toString() {
		String ret="Message(";
		if (m_objlist!=null) {
			ret+="m_objlist.size()="+m_objlist.size();
			int idx=0;
	 		for(Object i : m_objlist) {
				assert i instanceof ReqRes;
				ReqRes rr = (ReqRes) i;
				ret += ", m_objlist["+idx+"]="+rr.toString()+")";
				++idx;
			}
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
		return (m_name);
	}

	/** set request message name */
	public void setReqMsgName(String n) {
		m_reqmsgname = n;
	}

	/** get request message name */
	public String getReqMsgName() {
		return (m_reqmsgname);
	}

	/** set response message name */
	public void setRespMsgName(String n) {
		m_respmsgname = n;
	}

	/** get response message name */
	public String getRespMsgName() {
		return (m_respmsgname);
	}

	/** set completion time in MS */
	public void setCompletionTimeMS(int ms) {
		m_completiontimeMS=ms;
	}

	/** get completion time in MS */
	public int getCompletionTimeMS() {
		return(m_completiontimeMS);
	}

	/** Add an object to this message. The object must be a ReqRes */
	public void add(Object mo) {
		if(!(mo instanceof ReqRes))
			throw new IllegalArgumentException(
			    "dmslite.Message.add() wrong arg type.");
		m_objlist.add(mo);
	}

	/** Send a get request message */
	public void getRequest(String community) throws IOException {}

	/**
	 * Send a get request message
	 * @throws IOException if received response is malformed or timed out.
	 */
	public void getRequest() throws IOException {
		//Log.finest("dmslite.Message.getRequest() called.");

		// build message
		byte[] array = this.buildReqMsg();

		// send message
		long starttime=STime.getCurTimeUTCinMillis();
		Log.finest("getRequest(): Writing " + array.length + 
			" bytes to cmsserver.");
		m_is.resetBuffer();
		m_os.write(array);
		m_os.flush();

		// read response
		String token=null;
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
			String msg = "Unable to connect to cmsserver.";
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
			err += "Sent operation=" + 
				SString.byteArrayToString(array);
			Log.severe(err);
			throw new IOException(err);

		// parse response
		} else {
			Log.finest("dmslite.Message.getRequest(): found "+
				"complete token:" + token);

			// fill in returned fields for each ReqRes using received xml string
			for(Object i : m_objlist) {
				assert i instanceof ReqRes;
				ReqRes rr = (ReqRes) i;
				rr.parseRes(DMSLITEMSGTAG, getRespMsgName(),
			    		token);    // throws IOException on error
			}
		}
	}

	/** 
	  * Search for a request or response value by name.
	  * @return null if not found else the value.
	  */
	protected String searchForReqResItem(String name) {
		if (m_objlist==null)
			return null;
 		for(Object i : m_objlist) {
			assert i instanceof ReqRes;
			ReqRes rr = (ReqRes) i;
			String value=rr.searchReqResVal(name);
			if(value != null)
				return value;
		}
		return null;
	}

	/** Return true if message is owned by the AWS */
	protected boolean ownerIsAws() {
		String owner = this.searchForReqResItem("Owner");
		if(owner == null)
			return false;
		return OpDms.ownerIsAws(owner);
	}

	/** 
	  * Determine if failure sending an AWS message to the cmsserver
	  * occurred. 
	  * @return true on failure else false.
	  */
	protected boolean checkAwsFailure() {
		Log.finest("Message.checkAwsFailure() called. this=" + 
			toString() + ", ownerIsAws=" + ownerIsAws());
 		if(m_objlist == null)
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
		if(m_objlist == null)
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

	/** 
	  * This method handles a failure when IRIS fails to send an AWS 
	  * message to a DMS.
	  * @param errmsgnote Optional error message, appended to generated message.
	  */
	public void handleAwsFailure(String errmsgnote) {
		if(errmsgnote == null)
			errmsgnote = "";

		// owner is not aws?
		if(!ownerIsAws())
			return;

		// generate an error message
		String errmsg = getAwsFailureMessage() + errmsgnote;
		Log.warning("Warning: failure to send AWS message to DMS: " + errmsg);

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
				Log.finest("Message.handleAwsFailure(): sent email");
			}
			catch(MessagingException e) {
				Log.warning("Message.handleAwsFailure(): email "+
					"failed: " + e.getMessage());
			}
		}
	}

	/** Send a set request message */
	public void setRequest(String community) throws IOException {}

	/** Send an set request message */
	public void setRequest() throws IOException {}

	/**
	 * Return a request message with this format:
	 *     <DmsLite><msg name>...etc...</msg name></DmsLite>
	 */
	public byte[] buildReqMsg() {

		// build child tags
		StringBuilder children = new StringBuilder(256);

		for(Object i : m_objlist) {
			assert i instanceof ReqRes :
			       "dmslite.Message() arg must be a ReqRes";

			//Log.finest("will add:"+i);
			Xml.addXmlTag(children, ((ReqRes) i).getReqName(),
				((ReqRes) i).getReqVal());
		}

		// enclose child tags in message tag
		StringBuilder msgtag = new StringBuilder(384);

		Xml.addXmlTag(msgtag, this.getReqMsgName(), children);

		// enclose message tag in top level doc
		StringBuilder doc = new StringBuilder(384);

		Xml.addXmlTag(doc, DMSLITEMSGTAG, msgtag);
		Log.finest("dmslite.Message.buildReqMsg(): message to send is "
			+ doc.length() + " bytes,msg:" + doc.toString() + ".");

		byte[] array = doc.toString().getBytes();

		return (array);
	}
}
