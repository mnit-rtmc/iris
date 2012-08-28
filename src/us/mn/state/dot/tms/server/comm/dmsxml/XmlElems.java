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
import java.util.LinkedList;

/**
 * A container for multiple XmlElem objects.
 *
 * @author      Michael Darter
 */
class XmlElems
{
	/** Container for XmlElem objects */
	private final LinkedList<XmlElem> m_xelems = 
		new LinkedList<XmlElem>();

	/** Queue for intermediate status messages. Messages are enqueued
	 *  at the end of the list and dequeued from the start. */
	private final LinkedList<String> m_istatusqueue = 
		new LinkedList<String>();

	/** Constructor */
	XmlElems() {}

	/** toString */
	public String toString() {
		String ret = "XmlElems(";
		ret += "size=" + m_xelems.size();
		int i = 0;
 		for(XmlElem xe : m_xelems) {
			ret += ", m_xelems[" + i + "]=" + 
				xe.toString() + ")";
			++i;
		}
		ret += ")";
		return ret;
	}

	/** Parse response. The xml argument is parsed into the response 
	 *  fields contained within each XmlElem.
	 *  @param levelonetagname Xml string level one tag name.
	 *  @param istag Intermediate status tag name.
	 *  @param xml XML response string, may be null. */
	void parseResponse(String levelonetagname, String istag, 
		String xml) throws IOException
	{
		String childTag = Xml.readSecondTagName(
			levelonetagname, xml);
		if(childTag == null || childTag.isEmpty())
			return;
		boolean readinterstatus = false;
		for(XmlElem xe : m_xelems) {
			// tag in xml matches xml element?
			if(xe.resTagMatches(childTag)) {
				// throws IOException
				xe.parseResponse(levelonetagname, xml);
				xe.flagResRead();

				// if intermediate status update, enqueue msg
				if(xe.resTagMatches(istag))
					addInterStatusMsg(xe);
			}
		}
	}

	/** Enqueue an intermediate status message.
	 *  @param xe This XML element is assumed to be an intermediate 
	 *	   status message. */
	private void addInterStatusMsg(XmlElem xe) {
		m_istatusqueue.addLast(xe.getResString("Msg"));
	}

	/** Dequeue all intermediate status messages. Earlier messages
	 *  have lower indexes.
	 *  @return An array of String messages. */
	String[] getInterStatusMsgs() {
		String[] ret = new String[m_istatusqueue.size()];
		for(int i = 0; i < ret.length; ++i)
			ret[i] = m_istatusqueue.removeFirst();
		return ret;
	}

	/** Get response value for the specified request tag name.
	  * @return null if not found else the value. */
	String getResString(String reqname) {
 		for(XmlElem xe : m_xelems) {
			String value = xe.getResString(reqname);
			if(value != null)
				return value;
		}
		return null;
	}

	/** Add an element. */
	void add(XmlElem xe) {
		m_xelems.add(xe);
	}

	/** 
	 * Return a request XML message in this form:
	 *	<levelonetagname>
	 *		<msg 1 name>
	 *			...child elements...
	 *		</msg 1 name>
	 *		<msg 2 name>
	 *			...child elements...
	 *		</msg 2 name>
	 * 	</levelonetagname>
	 */
	byte[] buildReqMsg(String levelonetagname) {
		StringBuilder children = new StringBuilder();
 		for(XmlElem xe : m_xelems)
			if(xe.containsRequest())
				children.append(xe.buildReqMsg());

		// enclose elements in root element
		StringBuilder doc = new StringBuilder();
		Xml.addXmlTag(doc, levelonetagname, children);
		return doc.toString().getBytes();
	}

	/** Is element reading done? Element reading is done when a request 
	 *  element exists and its 'has been read' flag is true. */
	boolean readDone() {
		boolean done = false;
 		for(XmlElem xe : m_xelems) {
			if(xe.containsRequest() && xe.wasResRead()) {
				done = true;
				break;
			}
		}
		return done;
	}
}
