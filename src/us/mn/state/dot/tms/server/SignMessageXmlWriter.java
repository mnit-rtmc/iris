/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import us.mn.state.dot.sched.TimeSteward;
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
	static private final String SIGN_MESSAGE_XML = "sign_message.xml";

	/** Create a new sign message XML writer */
	public SignMessageXmlWriter() {
		super(SIGN_MESSAGE_XML, true);
	}

	/** Write the sign message XML file */
	@Override protected void write(Writer w) throws IOException {
		writeHead(w);
		writeBody(w);
		writeTail(w);
	}

	/** Write the head of the sign message XML file */
	private void writeHead(Writer w) throws IOException {
		w.write(XML_DECLARATION);
		writeDtd(w);
		w.write("<sign_messages time_stamp='" +
			TimeSteward.getDateInstance() + "'>\n");
	}

	/** Write the DTD */
	private void writeDtd(Writer w) throws IOException {
		w.write("<!DOCTYPE sign_messages [\n");
		w.write("<!ELEMENT sign_messages (sign_message)*>\n");
		w.write("<!ATTLIST sign_messages time_stamp CDATA #REQUIRED>\n");
		w.write("<!ELEMENT sign_message EMPTY>\n");
		w.write("<!ATTLIST sign_message dms CDATA #REQUIRED>\n");
		w.write("<!ATTLIST sign_message status CDATA #REQUIRED>\n");
		w.write("<!ATTLIST sign_message run_priority CDATA #IMPLIED>\n");
		w.write("<!ATTLIST sign_message act_priority CDATA #IMPLIED>\n");
		w.write("<!ATTLIST sign_message source CDATA #IMPLIED>\n");
		w.write("<!ATTLIST sign_message duration CDATA #IMPLIED>\n");
		w.write("<!ATTLIST sign_message incident CDATA #IMPLIED>\n");
		w.write("<!ATTLIST sign_message multi CDATA #REQUIRED>\n");
		w.write("<!ATTLIST sign_message bitmaps CDATA #IMPLIED>\n");
		w.write("<!ATTLIST sign_message deploy_time CDATA #IMPLIED>\n");
		w.write("]>\n");
	}

	/** Write the body of the sign message XML file */
	private void writeBody(Writer w) throws IOException {
		Iterator<DMS> it = DMSHelper.iterator();
		while(it.hasNext()) {
			DMS dms = it.next();
			if(dms instanceof DMSImpl)
				((DMSImpl)dms).writeSignMessageXml(w);
		}
	}

	/** Write the tail of the sign message XML file */
	private void writeTail(Writer w) throws IOException {
		w.write("</sign_messages>\n");
	}
}
