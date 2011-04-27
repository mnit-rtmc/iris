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
		super(XML_FILE, false);
	}

	/** Print the body of the incident list XML file */
	public void print(final PrintWriter out) {
		out.println(XML_DECLARATION);
		out.println("<active_incidents time_stamp='" +
			TimeSteward.getDateInstance() + "'>");
		IncidentHelper.find(new Checker<Incident>() {
			public boolean check(Incident inc) {
				if(inc instanceof IncidentImpl)
				   ((IncidentImpl)inc).printXmlElement(out);
				return false;
			}
		});
		out.println("</active_incidents>");
	}
}
