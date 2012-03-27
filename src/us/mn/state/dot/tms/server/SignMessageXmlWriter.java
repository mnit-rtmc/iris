/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
 * Copyright (C) 2012  Iteris Inc.
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
package us.mn.state.dot.tms.server;

import java.io.PrintWriter;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;

/**
 * This class writes out the current sign messages to an XML file.
 *
 * @author Tim Johnson
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignMessageXmlWriter extends XmlWriter {

	/** XML file */
	static protected final String SIGN_MESSAGE_XML = "sign_message.xml";

	/** Create a new sign message XML writer */
	public SignMessageXmlWriter() {
		super(SIGN_MESSAGE_XML, true);
	}

	/** Print the sign message XML file */
	public void print(final PrintWriter out) {
		printHead(out);
		printBody(out);
		printTail(out);
	}

	/** Print the head of the sign message XML file */
	protected void printHead(PrintWriter out) {
		out.println(XML_DECLARATION);
		printDtd(out);
		out.println("<sign_messages time_stamp='" +
			TimeSteward.getDateInstance() + "'>");
	}

	/** Print the DTD */
	protected void printDtd(PrintWriter out) {
		out.println("<!DOCTYPE sign_messages [");
		out.println("<!ELEMENT sign_messages (sign_message)*>");
		out.println("<!ATTLIST sign_messages time_stamp " +
			"CDATA #REQUIRED>");
		out.println("<!ELEMENT sign_message EMPTY>");
		out.println("<!ATTLIST sign_message dms CDATA #REQUIRED>");
		out.println("<!ATTLIST sign_message status CDATA #REQUIRED>");
		out.println("<!ATTLIST sign_message run_priority " + 
			"CDATA #IMPLIED>");
		out.println("<!ATTLIST sign_message act_priority " + 
			"CDATA #IMPLIED>");
		out.println("<!ATTLIST sign_message scheduled " + 
			"CDATA #IMPLIED>");
		out.println("<!ATTLIST sign_message duration " + 
			"CDATA #IMPLIED>");
		out.println("<!ATTLIST sign_message multi " + 
			"CDATA #REQUIRED>");
		out.println("<!ATTLIST sign_message bitmaps " + 
			"CDATA #IMPLIED>");
		out.println("]>");
	}

	/** Print the body of the sign message XML file */
	protected void printBody(final PrintWriter out) {
		DMSHelper.find(new Checker<DMS>() {
			public boolean check(DMS dms) {
				if(dms instanceof DMSImpl)
					((DMSImpl)dms).printSignMessageXml(out);
				return false;
			}
		});
	}

	/** Print the tail of the sign message XML file */
	protected void printTail(PrintWriter out) {
		out.println("</sign_messages>");
	}
}
