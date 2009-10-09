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
 * A container for multiple XmlReqRes objects.
 *
 * @author      Michael Darter
 * @created     10/09/09
 */
public class XmlReqResContainer
{
	/** Container for XmlReqRes objects */
	private final LinkedList<XmlReqRes> m_rrlist = 
		new LinkedList<XmlReqRes>();

	/** Constructor */
	public XmlReqResContainer() {}

	/** toString */
	public String toString() {
		String ret = "XmlReqResContainer(";
		ret += "size=" + m_rrlist.size();
		int i = 0;
 		for(XmlReqRes xrr : m_rrlist) {
			ret += ", m_rrlist[" + i + "]=" + 
				xrr.toString() + ")";
			++i;
		}
		ret += ")";
		return ret;
	}

	/** Parse response. */
	public void parseResponse(String levelonetagname, String xml) 
		throws IOException
	{
		for(XmlReqRes xmlrr : m_rrlist) {
			// throws IOException
			xmlrr.parseResponse(levelonetagname, xml);
		}
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	protected String getResValue(String reqname) {
 		for(XmlReqRes xmlrr : m_rrlist) {
			String value = xmlrr.getResValue(reqname);
			if(value != null)
				return value;
		}
		return null;
	}

	/** Add a request response. */
	public void add(XmlReqRes xmlrr) {
		m_rrlist.add(xmlrr);
	}

	/** 
	 * Return a request XML message in this form:
	 *	<level 1 name>
	 *		<msg 1 name>
	 *			...child elements...
	 *		</msg 1 name>
	 *		<msg 2 name>
	 *			...child elements...
	 *		</msg 2 name>
	 * 	</level 1 name>
	 */
	public byte[] buildReqMsg(String levelonetagname) {
		StringBuilder children = new StringBuilder();
 		for(XmlReqRes xmlrr : m_rrlist)
			children.append(xmlrr.buildReqMsg());

		// enclose elements in root element
		StringBuilder doc = new StringBuilder();
		Xml.addXmlTag(doc, levelonetagname, children);

		return doc.toString().getBytes();
	}
}
