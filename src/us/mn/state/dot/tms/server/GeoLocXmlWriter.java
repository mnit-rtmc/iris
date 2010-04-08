/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;

/**
 * This class writes out the current GeoLoc configuration to an XML file.
 *
 * @author Tim Johnson
 * @author Douglas Lau
 */
public class GeoLocXmlWriter extends XmlWriter {

	/** GeoLoc XML file */
	static protected final String GEOLOC_XML = "geoloc.xml";

	/** Create a new GeoLoc XML writer */
	public GeoLocXmlWriter() {
		super(GEOLOC_XML, false);
	}

	/** Print the body of the GeoLoc list XML file */
	public void print(final PrintWriter out) {
		out.println(XML_DECLARATION);
		out.println("<list>");
		GeoLocHelper.find(new Checker<GeoLoc>() {
			public boolean check(GeoLoc loc) {
				printXmlElement(loc, out);
				return false;
			}
		});
		out.println("</list>");
	}

	/** Render the GeoLoc object as xml */
	protected void printXmlElement(GeoLoc p, PrintWriter out) {
		out.print("<" + p.SONAR_TYPE);
		out.print(createAttribute("id", p.getName()));
		out.print(createAttribute("northing",
			GeoLocHelper.getTrueNorthing(p)));
		out.print(createAttribute("easting",
			GeoLocHelper.getTrueEasting(p)));
		out.print(createAttribute("roadway", p.getRoadway()));
		if(p.getCrossStreet() != null) {
			out.print(createAttribute("cross_mod",
				GeoLocHelper.getModifier(p)));
			out.print(createAttribute("cross_street",
				p.getCrossStreet()));
		}
		out.println("/>");
	}
}
