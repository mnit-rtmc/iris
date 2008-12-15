/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Properties;
import us.mn.state.dot.tms.MainServer;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.caws.CawsPoller;
import us.mn.state.dot.tms.utils.SString;
import us.mn.state.dot.tms.utils.STime;
import us.mn.state.dot.tms.utils.SEmail;
import us.mn.state.dot.tms.utils.PropertyFile;

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
	protected final LinkedList<Object> m_objlist = new LinkedList<Object>();
	private final TokenStreamReader m_is;
	private final OutputStream m_os;

	/** Create a new message */
	public Message(OutputStream os, InputStream is) {
		m_os = os;
		m_is = new TokenStreamReader(
		    is, 1024, 16384, 1000);    // buffer size, max cap, sleep time
		//System.err.println("dmslite.Message.Message() called.");
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
		System.err.println("DmsLite.Message.setTimeoutMS("+ms+") called.");
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
	 *
	 * @throws IOException if received response is malformed or timed out.
	 */
	public void getRequest() throws IOException {
		System.err.println("dmslite.Message.getRequest() called.");

		// build message
		byte[] array = this.buildReqMsg();

		// send message
		long starttime=STime.getCurTimeUTCinMillis();
		System.err.print("Writing " + array.length+" bytes to cmsserver....");
		m_is.resetBuffer();
		m_os.write(array);
		m_os.flush();
		System.err.println("write done.");

		// read response
		String token=null;
		try {
			token = m_is.readToken(m_dmsTimeoutMS,
		       	"<" + DMSLITEMSGTAG + ">","</" + DMSLITEMSGTAG + ">");
			this.setCompletionTimeMS((int)STime.calcTimeDeltaMS(starttime));
		} catch (IllegalStateException ex) {
			this.handleCAWSFailure("Illegal state reading response from cmsserver.");
			throw new IOException(
			    "SEVERE error: capacity exceeded in dmslite.Message.getRequest(): "+ex);
		} catch (IOException ex) {
			throw new IOException("CMS disconnected:" + ex);
		}

		// timed out?
		if(token == null) {
			this.handleCAWSFailure("Possibly timed out waiting for response from cmserver.");
			String err="";
			err+="SEVERE error: dmslite.Message.getRequest(): timed out waiting for CMS ("+
			    (getCompletionTimeMS()/1000)+"seconds). Timeout is "+m_dmsTimeoutMS / 1000 + " secs). ";
			System.err.println(err);
			System.err.println("Sent operation="+SString.byteArrayToString(array));
			throw new IOException(err);

		// parse response
		} else {
			System.err.println(
			    "dmslite.Message.getRequest(): found complete token:"
			    + token);

			// fill in returned fields for each ReqRes using received xml string
			for(Object i : m_objlist) {
				assert i instanceof ReqRes;
				ReqRes rr = (ReqRes) i;
				rr.parseRes(DMSLITEMSGTAG,this.getRespMsgName(),
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

	/** return true if message owner is CAWS */
	protected boolean ownerCaws() {
		String owner=this.searchForReqResItem("Owner");
		if(owner == null)
			return false;
		return owner.toLowerCase().equals(
			CawsPoller.CAWS_OWNER.toLowerCase());
	}

	/** 
	  * Determine if a failure sending a CAWS message to the cmsserver
	  * occurred. 
	  * @return true on failure else false.
	  */
	protected boolean checkCAWSFailure() {
		System.err.println("Message.checkCAWSFailure() called. this="+this.toString()+", ownerCAWS="+ownerCaws());
		if (m_objlist==null)
			return false;

		String ret=null;

		// IsValid: was there an error?
		String isvalid=this.searchForReqResItem("IsValid");
		if(isvalid == null || isvalid.toLowerCase().equals("true"))
			return false;

		// owner isn't caws?
		if(!ownerCaws())
			return false;

		// at this point we know there was an error to report
		return true;
	}

	/** Generate a CAWS failure message */
	protected String getCAWSFailureMessage() {
		if (m_objlist==null)
			return "";

		String ret="";

		// Owner: was owner CAWS?
		String owner=this.searchForReqResItem("Owner");
		if(owner == null)
			owner="";

		// ErrMsg: get the error description
		String errmsg=this.searchForReqResItem("ErrMsg");
		if(errmsg == null)
			errmsg="";

		// Id: get message id
		String id=this.searchForReqResItem("Id");
		if(id == null)
			id="";

		// Address: get cms number
		String address=this.searchForReqResItem("Address");
		if(address == null)
			address="";

		// MsgText: actual message
		String msg=this.searchForReqResItem("MsgText");
		if(msg == null)
			msg="";

		// build error string
		ret="";
		ret+="Could not send a CAWS message to a CMS: reason="+errmsg;
		ret+=", CMS="+address;
		ret+=", message id="+id;
		ret+=", time="+STime.getCurDateTimeString(true);
		ret+=", message="+msg;
		ret+=", author="+owner;
		ret+=", note=";	// appended to by handleCawsFailure()

		//System.err.println("Message.getCAWSFailureMessage() returning="+ret);
		return ret;
	}

	/** 
	  * This method handles a failure when IRIS fails to send a CAWS 
	  * message to a CMS.
	  * @param errmsgnote Optional error message, appended to generated message.
	  */
	public void handleCAWSFailure(String errmsgnote) {
		if(MainServer.m_serverprops==null)
			return;
		if(MainServer.m_serverprops==null)
			return;
		if(errmsgnote==null)
			errmsgnote="";

		// owner isn't caws?
		if(!ownerCaws())
			return;

		// generate an error message
		String errmsg = getCAWSFailureMessage() + errmsgnote;
		System.err.println("Warning: failure to send CAWS message to CMS: "+errmsg);

		// build email
		Properties props = MainServer.getServerProps();
		String sender=PropertyFile.get(props,"d10.caws.email_sender");
		String recipient=PropertyFile.get(props,"d10.caws.email_recipient");
		String subject="IRIS could not send CAWS message to CMS";

		// send
		if(recipient==null || sender==null || recipient.length()<=0 || 
			sender.length()<=0)
			System.err.println("Message.handleCAWSFailure(): didn't try to send CAWS error email.");
		else {
			boolean sendflag=SEmail.sendEmail(props, sender, 
				recipient, subject, errmsg);
			System.err.println("Message.handleCAWSFailure(): sent email, success="+sendflag);
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

			// System.err.println("will add:"+i);
			Xml.addXmlTag(children, ((ReqRes) i).getReqName(),
				((ReqRes) i).getReqVal());
		}

		// enclose child tags in message tag
		StringBuilder msgtag = new StringBuilder(384);

		Xml.addXmlTag(msgtag, this.getReqMsgName(), children);

		// enclose message tag in top level doc
		StringBuilder doc = new StringBuilder(384);

		Xml.addXmlTag(doc, DMSLITEMSGTAG, msgtag);
		System.err.println(
		    "dmslite.Message.buildReqMsg(): message to send is "
		    + doc.length() + " bytes, msg:" + doc.toString() + ".");

		byte[] array = doc.toString().getBytes();

		return (array);
	}
}
