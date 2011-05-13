/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentHelper;

/**
 * This class writes out the active incidents to an XML file.
 *
 * @author Douglas Lau
 */
public class IncidentXmlWriter extends XmlWriter {

	/** Incident XML file */
	static protected final String XML_FILE = "incident.xml";

	/** Create a new incident XML writer */
	public IncidentXmlWriter() {
		super(XML_FILE, true);
	}

	/** Print the incident XML file */
	public void print(final PrintWriter out) {
		printHead(out);
		printBody(out);
		printTail(out);
	}

	/** Print the head of the incident XML file */
	protected void printHead(PrintWriter out) {
		out.println(XML_DECLARATION);
		printDtd(out);
		out.println("<active_incidents time_stamp='" +
			TimeSteward.getDateInstance() + "'>");
	}

	/** Print the DTD */
	protected void printDtd(PrintWriter out) {
		out.println("<!DOCTYPE active_incidents [");
		out.println("<!ELEMENT active_incidents (incident)*>");
		out.println("<!ATTLIST active_incidents time_stamp " +
			"CDATA #REQUIRED>");
		out.println("<!ELEMENT incident EMPTY>");
		out.println("<!ATTLIST incident name CDATA #REQUIRED>");
		out.println("<!ATTLIST incident event_type CDATA #REQUIRED>");
		out.println("<!ATTLIST incident event_date CDATA #REQUIRED>");
		out.println("<!ATTLIST incident detail CDATA #IMPLIED>");
		out.println("<!ATTLIST incident lane_type CDATA #REQUIRED>");
		out.println("<!ATTLIST incident road CDATA #REQUIRED>");
		out.println("<!ATTLIST incident dir CDATA #REQUIRED>");
		out.println("<!ATTLIST incident location CDATA #IMPLIED>");
		out.println("<!ATTLIST incident lon CDATA #REQUIRED>");
		out.println("<!ATTLIST incident lat CDATA #REQUIRED>");
		out.println("<!ATTLIST incident camera CDATA #REQUIRED>");
		out.println("<!ATTLIST incident impact CDATA #REQUIRED>");
		out.println("<!ATTLIST incident cleared CDATA #REQUIRED>");
		out.println("]>");
	}

	/** Print the body of the incident XML file */
	protected void printBody(final PrintWriter out) {
		IncidentHelper.find(new Checker<Incident>() {
			public boolean check(Incident inc) {
				if(inc instanceof IncidentImpl)
					((IncidentImpl)inc).printXml(out);
				return false;
			}
		});
	}

	/** Print the tail of the incident XML file */
	protected void printTail(PrintWriter out) {
		out.println("</active_incidents>");
	}
}
