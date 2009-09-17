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
package us.mn.state.dot.tms.server;

import java.io.PrintWriter;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;

/**
 * This class writes out the current DMS configuration and state to an XML file.
 *
 * @author Tim Johnson
 * @author Douglas Lau
 */
public class DMSXmlWriter extends XmlWriter {

	/** DMS XML file */
	static protected final String DMS_XML = "dms.xml";

	/** Create a new DMS XML writer */
	public DMSXmlWriter() {
		super(DMS_XML, false);
	}

	/** Print the body of the DMS list XML file */
	public void print(final PrintWriter out) {
		out.println(XML_DECLARATION);
		out.println("<list>");
		DMSHelper.find(new Checker<DMS>() {
			public boolean check(DMS dms) {
				if(dms instanceof DMSImpl)
					((DMSImpl)dms).printXmlElement(out);
				return false;
			}
		});
		out.println("</list>");
	}
}
