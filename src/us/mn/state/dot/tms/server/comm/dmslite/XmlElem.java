/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import us.mn.state.dot.tms.utils.STime;

/**
 * This class represents an XML element that contains child elements one
 * level deep. These child elements are considered 'request' elements. These
 * child elements are used to generate XML that is sent to a process, which
 * replies with an XML element containing child elements, also one level 
 * deep. These are child elements are considered 'response' elements and are
 * also stored by this class.
 * @author Michael Darter
 * @created 10/09/09
 */
public class XmlElem
{
	/** Top-level element request name */
	private String m_reqname = "";

	/** Top-level element response name */
	private String m_resname = "";

	/** Children request elements */
	private final HashMap<String, Object> m_reqlist = 
		new HashMap<String, Object>();

	/** Children response elements */
	private final HashMap<String, Object> m_reslist = 
		new HashMap<String, Object>();

	/** Constructor.
	 * @param reqname XML tag name for request root element.
	 * @param resname XML tag name for response root element. */
	public XmlElem(String reqname, String resname) {
		m_reqname = reqname;
		m_resname = resname;
	}

	/** Parse response using XML string and fill in response fields. */
	public void parseResponse(String levelonetagname, String xml) 
		throws IOException
	{
		// generate array of pairs
		Pair[] p;
		try {
			p = Xml.parseTagsAndChildren(levelonetagname, 
				m_resname, xml);
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
	public String buildReqMsg() {
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
		Xml.addXmlTag(msgtag, m_reqname, children);
		return msgtag.toString();
	}

	/** Get response value for the specified response name.
	  * @return null if not found else the value. */
	private Object getResValue(String resname) {
		if(resname == null)
			return null;
		return m_reslist.get(resname);
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	protected String getResString(String resname) {
		Object ret = getResValue(resname);
		if(ret == null)
			return null;
		return ret.toString();
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	protected long getResLong(String resname) {
		String ret = getResString(resname);
		if(ret == null)
			return 0;
		return new Long(ret);
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	protected int getResInt(String resname) {
		String ret = getResString(resname);
		if(ret == null)
			return 0;
		return new Integer(ret);
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	protected boolean getResBoolean(String resname) {
		String ret = getResString(resname);
		if(ret == null)
			return false;
		return new Boolean(ret);
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	protected Date getResDate(String resname) {
		String ret = getResString(resname);
		if(ret == null)
			return null;
		return STime.XMLtoDate(ret);
	}

	/** Add a request. */
	public void addReq(String name, Object value) {
		m_reqlist.put(name, value);
	}

	/** Add a response. */
	public void addRes(String name) {
		m_reslist.put(name, "");
	}

	/** Add a response. */
	public void addRes(String name, Object value) {
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
}
