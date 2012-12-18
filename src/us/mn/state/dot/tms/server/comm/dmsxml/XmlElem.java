/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.utils.SString;

/**
 * This class represents an XML element that contains child elements one
 * level deep. These child elements are considered 'request' elements. These
 * child elements are used to generate XML that is sent to a process, which
 * replies with an XML element containing child elements, also one level 
 * deep. These are child elements are considered 'response' elements and are
 * also stored by this class.
 * FIXME: convert to use ControllerProperty encode/decode methods.
 *
 * @author Michael Darter
 */
class XmlElem extends ControllerProperty {

	/** Top-level element request tag name */
	private String m_reqtagname = "";

	/** Top-level element response tag name */
	private String m_restagname = "";

	/** Children request elements */
	private final HashMap<String, Object> m_reqlist = 
		new HashMap<String, Object>();

	/** Children response elements */
	private final HashMap<String, Object> m_reslist = 
		new HashMap<String, Object>();

	/** Flag if response has been read */
	private boolean m_resread = false;

	/** Constructor.
	 * @param reqtagname XML tag name for request root element.
	 * @param restagname XML tag name for response root element. */
	XmlElem(String reqtagname, String restagname) {
		m_reqtagname = (reqtagname == null ? "" : reqtagname);
		m_restagname = (restagname == null ? "" : restagname);
	}

	/** toString */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("XmlElem(");
		sb.append("req size=").append(m_reqlist.size()).append(", ");
		sb.append("res size=").append(m_reslist.size()).append(", ");
		sb.append("reqtagname=").append(m_reqtagname).append(", ");
		sb.append("restagname=").append(m_restagname).append(", ");
		sb.append("resread=").append(m_resread);
		sb.append(")");
		return sb.toString();
	}

	/** Parse response using XML string and fill in response fields. */
	void parseResponse(String levelonetagname, String xml) 
		throws IOException
	{
		// generate array of pairs
		Pair[] p;
		try {
			p = Xml.parseTagsAndChildren(levelonetagname, 
				m_restagname, xml);
		} catch (IOException ex) {
			throw ex;
		}

		// cycle through pairs, updating response fields
		for(Pair i : p) {
			String n = (String) (i.car());
			String v = (String) (i.cdr());
			updateResVal(n, v);
		}
	}

	/** Return a request XML message with this format:
	 *  <msg name>...child elements...</msg name> */
	String buildReqMsg() {
		// build child tags
		StringBuilder children = new StringBuilder(256);
		Iterator it = m_reqlist.keySet().iterator();
		while(it.hasNext()) {
			String n = (String)it.next();
			Object v = m_reqlist.get(n);
			if(n != null && v != null)
				Xml.addXmlTag(children, n, v.toString());
		} 
		// enclose child tags in message tag
		StringBuilder msgtag = new StringBuilder(384);
		Xml.addXmlTag(msgtag, m_reqtagname, children);
		return msgtag.toString();
	}

	/** Get response value for the specified response name.
	  * @return null if not found else the value. */
	Object getResValue(String resname) {
		if(resname == null)
			return null;
		return m_reslist.get(resname);
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	String getResString(String resname) {
		Object ret = getResValue(resname);
		if(ret == null)
			return null;
		return ret.toString();
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	long getResLong(String resname) {
		String ret = getResString(resname);
		if(ret == null)
			return 0;
		return SString.stringToLong(ret);
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	int getResInt(String resname) {
		String ret = getResString(resname);
		if(ret == null)
			return 0;
		return SString.stringToInt(ret);
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	boolean getResBoolean(String resname) {
		String ret = getResString(resname);
		if(ret == null)
			return false;
		return SString.stringToBoolean(ret);
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	Date getResDate(String resname) {
		String ret = getResString(resname);
		if(ret == null)
			return null;
		return STime.XMLtoDate(ret);
	}

	/** Add a request. */
	void addReq(String name, Object value) {
		m_reqlist.put(name, value);
	}

	/** Add a response. */
	void addRes(String name) {
		m_reslist.put(name, "");
	}

	/** Add a response. */
	void addRes(String name, Object value) {
		m_reslist.put(name, value);
	}

	/** Update a response field. */
	private void updateResVal(String resname, Object resval) {
		if(resname == null || resname.isEmpty())
			return;
		Iterator it = m_reslist.keySet().iterator();
		while(it.hasNext()) {
			String n = (String)it.next();
			if(n.equals(resname))
				m_reslist.put(resname, resval);
		} 
	}

	/** Does the specified tag name match the name of the expected
	 *  response tag? */
	boolean resTagMatches(String restagname) {
		return m_restagname.equals(restagname);
	}

	/** Return the response name */
	String getResTagName() {
		return m_restagname;
	}

	/** Does the element contain any requests? */
	boolean containsRequest() {
		return m_reqlist.size() > 0;
	}

	/** Flag response has been read */
	void flagResRead() {
		m_resread = true;
	}

	/** Has response been read? */
	boolean wasResRead() {
		return m_resread;
	}
}
