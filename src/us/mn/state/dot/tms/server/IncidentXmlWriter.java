/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
import java.util.Date;
import java.util.Iterator;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentHelper;

/**
 * This class writes out the active incidents to an XML file.
 *
 * @author Douglas Lau
 */
public class IncidentXmlWriter extends XmlWriter {

	/** Incident XML file */
	static private final String XML_FILE = "incident.xml";

	/** Create a new incident XML writer */
	public IncidentXmlWriter() {
		super(XML_FILE, true);
	}

	/** Write the incident XML file */
	@Override protected void write(Writer w) throws IOException {
		writeHead(w);
		writeBody(w);
		writeTail(w);
	}

	/** Write the head of the incident XML file */
	private void writeHead(Writer w) throws IOException {
		w.write(XML_DECLARATION);
		writeDtd(w);
		w.write("<active_incidents time_stamp='" +
			TimeSteward.getDateInstance() + "'>\n");
	}

	/** Write the DTD */
	private void writeDtd(Writer w) throws IOException {
		w.write("<!DOCTYPE active_incidents [\n");
		w.write("<!ELEMENT active_incidents (incident)*>\n");
		w.write("<!ATTLIST active_incidents time_stamp " +
			"CDATA #REQUIRED>\n");
		w.write("<!ELEMENT incident EMPTY>\n");
		w.write("<!ATTLIST incident name CDATA #REQUIRED>\n");
		w.write("<!ATTLIST incident replaces CDATA #IMPLIED>\n");
		w.write("<!ATTLIST incident event_type CDATA #REQUIRED>\n");
		w.write("<!ATTLIST incident event_date CDATA #REQUIRED>\n");
		w.write("<!ATTLIST incident detail CDATA #IMPLIED>\n");
		w.write("<!ATTLIST incident lane_type CDATA #REQUIRED>\n");
		w.write("<!ATTLIST incident road CDATA #REQUIRED>\n");
		w.write("<!ATTLIST incident dir CDATA #REQUIRED>\n");
		w.write("<!ATTLIST incident location CDATA #IMPLIED>\n");
		w.write("<!ATTLIST incident lon CDATA #REQUIRED>\n");
		w.write("<!ATTLIST incident lat CDATA #REQUIRED>\n");
		w.write("<!ATTLIST incident camera CDATA #REQUIRED>\n");
		w.write("<!ATTLIST incident impact CDATA #REQUIRED>\n");
		w.write("<!ATTLIST incident cleared CDATA #REQUIRED>\n");
		w.write("]>\n");
	}

	/** Write the body of the incident XML file */
	private void writeBody(Writer w) throws IOException {
		Iterator<Incident> it = IncidentHelper.iterator();
		while(it.hasNext()) {
			Incident inc = it.next();
			if(inc instanceof IncidentImpl)
				((IncidentImpl)inc).writeXml(w);
		}
	}

	/** Write the tail of the incident XML file */
	private void writeTail(Writer w) throws IOException {
		w.write("</active_incidents>\n");
	}
}
