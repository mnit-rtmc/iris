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
import java.util.LinkedList;

/**
 * This class represents a request and a response in the form of an XML 
 * element that contains child elements 1 level deep. Each child element
 * may have an associated response.
 *
 * @author      Michael Darter
 * @created     10/09/09
 */
public class XmlReqRes
{
	/** Top-lvel element request name */
	private String m_reqname = "";

	/** Top-level element response name */
	private String m_resname = "";

	/** Children request elements and their responses */
	private final LinkedList<ReqRes> m_objlist = 
		new LinkedList<ReqRes>();

	/** Constructor.
	 * @param reqname XML tag name for request root element.
	 * @param resname XML tag name for response root element. */
	public XmlReqRes(String reqname, String resname) {
		m_reqname = reqname;
		m_resname = resname;
	}

	/** toString */
	public String toString() {
		String ret = "XmlReqRes(";
		if(m_objlist != null) {
			ret += "size=" + m_objlist.size();
			int i = 0;
	 		for(ReqRes rr : m_objlist) {
				ret += ", m_objlist[" + i + "]=" + 
					rr.toString() + ")";
				++i;
			}
		}
		ret += ")";
		return ret;
	}

	/** Parse response using XML string and fill in response fields. */
	public void parseResponse(String levelonetagname, String xml) 
		throws IOException
	{
		if(m_objlist == null)
			return;
		for(ReqRes rr : m_objlist) {
			// throws IOException
			rr.parseRes(levelonetagname, m_resname, xml);
		}
	}

	/** 
	 * Return a request XML message with this format:
	 *	<level 1 name><msg name>
	 *		...child elements...
	 *	</msg name></level 1 name>
	 */
	public byte[] buildReqMsg(String levelonetagname) {

		// build child tags
		StringBuilder children = new StringBuilder(256);
		for(ReqRes rr : m_objlist)
			Xml.addXmlTag(children, rr.getReqName(), 
				rr.getReqVal());

		// enclose child tags in message tag
		StringBuilder msgtag = new StringBuilder(384);
		Xml.addXmlTag(msgtag, m_reqname, children);

		// enclose message tag in top level doc
		StringBuilder doc = new StringBuilder(384);

		Xml.addXmlTag(doc, levelonetagname, msgtag);
		byte[] ret = doc.toString().getBytes();
		return ret;
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	protected String getResponseValue(String reqname) {
		if(m_objlist==null)
			return null;
 		for(ReqRes rr : m_objlist) {
			String value = rr.searchReqResVal(reqname);
			if(value != null)
				return value;
		}
		return null;
	}

	/** Add a request response. */
	public void add(ReqRes rr) {
		m_objlist.add(rr);
	}
}
