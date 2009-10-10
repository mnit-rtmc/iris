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
 * A container for multiple XmlElem objects.
 *
 * @author      Michael Darter
 * @created     10/09/09
 */
public class XmlElems
{
	/** Container for XmlElem objects */
	private final LinkedList<XmlElem> m_rrlist = 
		new LinkedList<XmlElem>();

	/** Constructor */
	public XmlElems() {}

	/** toString */
	public String toString() {
		String ret = "XmlElems(";
		ret += "size=" + m_rrlist.size();
		int i = 0;
 		for(XmlElem xrr : m_rrlist) {
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
		for(XmlElem xmlrr : m_rrlist) {
			// throws IOException
			xmlrr.parseResponse(levelonetagname, xml);
		}
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	protected String getResString(String reqname) {
 		for(XmlElem xmlrr : m_rrlist) {
			String value = xmlrr.getResString(reqname);
			if(value != null)
				return value;
		}
		return null;
	}

	/** Add an element. */
	public void add(XmlElem xmlrr) {
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
 		for(XmlElem xmlrr : m_rrlist)
			children.append(xmlrr.buildReqMsg());

		// enclose elements in root element
		StringBuilder doc = new StringBuilder();
		Xml.addXmlTag(doc, levelonetagname, children);

		return doc.toString().getBytes();
	}
}
