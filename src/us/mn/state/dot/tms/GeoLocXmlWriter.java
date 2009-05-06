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
package us.mn.state.dot.tms;

import java.io.PrintWriter;

import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.server.ServerNamespace;

/**
 * This class writes out the current GeoLoc configuration to an XML file.
 *
 * @author Tim Johnson
 */
public class GeoLocXmlWriter extends XmlWriter {

	/** GeoLoc XML file */
	static protected final String GEOLOC_XML = "geoloc.xml";

	/** SONAR namespace */
	protected final ServerNamespace namespace;

	/** Create a new GeoLoc XML writer */
	public GeoLocXmlWriter(ServerNamespace ns) {
		super(GEOLOC_XML, false);
		namespace = ns;
	}

	/** Print the body of the GeoLoc list XML file */
	public void print(final PrintWriter out) {
		out.println(XML_DECLARATION);
		out.println("<geoloc_list>");
		namespace.findObject(GeoLoc.SONAR_TYPE,
			new Checker<GeoLocImpl>()
		{
			public boolean check(GeoLocImpl loc) {
				GeoLocHelper.printXmlElement(loc, out);
				return false;
			}
		});
		out.println("</geoloc_list>");
	}
}
