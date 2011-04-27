/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2011  Minnesota Department of Transportation
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
import java.io.PrintWriter;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;

/**
 * This class writes out the current detector configuration to an XML file.
 *
 * @author Douglas Lau
 */
public class DetectorXmlWriter extends XmlWriter {

	/** Detector list XML file */
	static protected final String DETS_XML = "detectors.xml";

	/** Create a new detector XML writer */
	public DetectorXmlWriter() {
		super(DETS_XML, false);
	}

	/** Print the DTD for detector elements */
	public void printDtd(PrintWriter out) {
		out.println("<!ELEMENT detector EMPTY>");
		out.println("<!ATTLIST detector index ID #REQUIRED>");
		out.println("<!ATTLIST detector label CDATA 'FUTURE'>");
		out.println("<!ATTLIST detector abandoned CDATA 'f'>");
		out.println("<!ATTLIST detector category CDATA ''>");
		out.println("<!ATTLIST detector lane CDATA '0'>");
		out.println("<!ATTLIST detector field CDATA '22.0'>");
	}

	/** Print the body of the detector list XML file */
	public void print(final PrintWriter out) {
		DetectorHelper.find(new Checker<Detector>() {
			public boolean check(Detector d) {
				if(d instanceof DetectorImpl)
					((DetectorImpl)d).printXmlElement(out);
				return false;
			}
		});
	}
}
