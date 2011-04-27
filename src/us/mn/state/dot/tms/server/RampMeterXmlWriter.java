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

import java.io.IOException;
import java.io.PrintWriter;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;

/**
 * This class writes out the current ramp meter configuration to an XML file.
 *
 * @author Douglas Lau
 */
public class RampMeterXmlWriter extends XmlWriter {

	/** Ramp meter list XML file */
	static protected final String METER_XML = "ramp_meters.xml";

	/** Create a new ramp meter XML writer */
	public RampMeterXmlWriter() {
		super(METER_XML, false);
	}

	/** Print the DTD for ramp meter elements */
	public void printDtd(PrintWriter out) {
		out.println("<!ELEMENT meter EMPTY>");
		out.println("<!ATTLIST meter name CDATA #REQUIRED>");
		out.println("<!ATTLIST meter label CDATA #REQUIRED>");
		out.println("<!ATTLIST meter storage CDATA #REQUIRED>");
		out.println("<!ATTLIST meter max_wait CDATA '" +
			RampMeterImpl.DEFAULT_MAX_WAIT +"'>");
		out.println("<!ATTLIST meter green CDATA #IMPLIED>");
		out.println("<!ATTLIST meter passage CDATA #IMPLIED>");
		out.println("<!ATTLIST meter merge CDATA #IMPLIED>");
		out.println("<!ATTLIST meter queue CDATA #IMPLIED>");
		out.println("<!ATTLIST meter bypass CDATA #IMPLIED>");
	}

	/** Print the body of the ramp meter list XML file */
	public void print(final PrintWriter out) {
		RampMeterHelper.find(new Checker<RampMeter>() {
			public boolean check(RampMeter m) {
				if(m instanceof RampMeterImpl)
					((RampMeterImpl)m).printXmlElement(out);
				return false;
			}
		});
	}
}
