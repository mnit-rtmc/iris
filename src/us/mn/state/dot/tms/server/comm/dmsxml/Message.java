/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;
import static us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller.LOG;
import us.mn.state.dot.tms.utils.SString;

/**
 * DMS XML message. A Message represents the bytes sent and
 * received from a device. The interface is intended to be
 * flexible enough so that a single class can be used for
 * all message types.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
class Message implements CommMessage {

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
		if (m_xelems != null)
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
		return m_completiontimeMS;
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
		if (!(xmlrr instanceof XmlElem))
			throw new IllegalArgumentException(
			    "dmsxml.Message.add() wrong arg type.");
		m_xelems.add((XmlElem) xmlrr);
	}

	/** Set the associated operation */
	public void setOperation(OpDms op) {
		m_opdms = op;
	}

	/** Query the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void queryProps() throws IOException {
		long starttime = sendRequest();
		readElements(starttime);
	}

	/** Send a request */
	private long sendRequest() throws IOException {
		// build message
		byte[] array = buildReqMsg();

		// send message
		long starttime = TimeSteward.currentTimeMillis();
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
		return (m_opdms != null) ? m_opdms.m_dms.getName() : "V?";
	}

	/** Read one XML element in the response. */
	private void readElements(long starttime) throws IOException {
		long startms = TimeSteward.currentTimeMillis();
		do {
			String token = null;
			try {
				long elapsed = STime.calcTimeDeltaMS(startms);
				int leftms = (int) (m_dmsTimeoutMS - elapsed);
				if (leftms > 0) {
					token = m_is.readToken(leftms,
					       	"<" + DMSXMLMSGTAG + ">",
						"</" + DMSXMLMSGTAG + ">");
					setCompletionTimeMS((int) STime.
						calcTimeDeltaMS(starttime));
					LOG.log("Response received in " +
						getCompletionTimeMS() +
						" ms.");
				}
			}
			catch (IllegalStateException ex) {
				throw new ParsingException(
					"buffer cap exceeded");
			}
			catch (IOException ex) {
				LOG.log("WARNING: " + ex.getMessage());
				throw ex;
			}

			// time has expired
			if (token == null) {
				String dmsid = getDmsId();
				String err = "";
				err += "dmsxml.Message.readElements(): " +
					"timed out waiting for " + dmsid + "("
					+ (getCompletionTimeMS() / 1000) +
					" seconds). Timeout is " +
					m_dmsTimeoutMS / 1000 + " secs). ";
				LOG.log(err);
				throw new ParsingException("NO RESPONSE");
			}

			// parse response
			LOG.log("dmsxml.Message.queryProps(): " +
				"found complete token:" + token);

			// sets 'was read' flag for each XML element
			m_xelems.parseResponse(Message.DMSXMLMSGTAG,
				Message.ISTATUSTAG, token);
		}
		while (!m_xelems.readDone());
	}

	/** Get the response value by name.
	  * @return null if not found else the value. */
	protected String getResString(String name) {
		return (m_xelems != null) ? m_xelems.getResString(name) : null;
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	public void storeProps() throws IOException {}

	/** Return a request message with this format:
	 *     <DmsXml><msg name>...etc...</msg name></DmsXml> */
	public byte[] buildReqMsg() {
		if (m_xelems == null)
			return new byte[0];
		return m_xelems.buildReqMsg(DMSXMLMSGTAG);
	}
}
