/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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
import java.util.Properties;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;

/**
 * This class writes out the TMS configuration data to an XML file.
 *
 * @author Douglas Lau
 */
public class TmsConfigXmlWriter extends XmlWriter {

	/** TMS config XML file */
	static protected final String CONFIG_XML = "_config.xml";

	/** Agency district property */
	static protected String district = "tms";

	/** Initialize the district property */
	static public void init(Properties props) {
		district = props.getProperty("district", "tms");
	}

	/** R_Node XML writer */
	protected final R_NodeXmlWriter node_writer = new R_NodeXmlWriter();

	/** Ramp meter XML writer */
	protected final RampMeterXmlWriter meter_writer =
		new RampMeterXmlWriter();

	/** Camera XML writer */
	protected final CameraXmlWriter cam_writer = new CameraXmlWriter();

	/** Create a new TMS config XML writer */
	public TmsConfigXmlWriter() {
		super(district + CONFIG_XML, true);
	}

	/** Print the TMS config XML file */
	public void print(final PrintWriter out) {
		printHead(out);
		printBody(out);
		printTail(out);
	}

	/** Print the head of the TMS config XML file */
	protected void printHead(PrintWriter out) {
		out.println(XML_DECLARATION);
		printDtd(out);
		out.println("<tms_config time_stamp='" +
			TimeSteward.getDateInstance() + "'>");
	}

	/** Print the DTD */
	protected void printDtd(PrintWriter out) {
		out.println("<!DOCTYPE tms_config [");
		out.println("<!ELEMENT tms_config (corridor | camera | dms)*>");
		out.println("<!ATTLIST tms_config time_stamp CDATA #REQUIRED>");
		node_writer.printDtd(out);
		printCameraDtd(out);
		printDmsDtd(out);
		out.println("]>");
	}

	/** Print the DTD for camera elements */
	protected void printCameraDtd(PrintWriter out) {
		out.println("<!ELEMENT camera EMPTY>");
		out.println("<!ATTLIST camera name CDATA #REQUIRED>");
		out.println("<!ATTLIST camera description CDATA #REQUIRED>");
		out.println("<!ATTLIST camera lon CDATA #IMPLIED>");
		out.println("<!ATTLIST camera lat CDATA #IMPLIED>");
	}

	/** Print the DTD for DMS elements */
	protected void printDmsDtd(PrintWriter out) {
		out.println("<!ELEMENT dms EMPTY>");
		out.println("<!ATTLIST dms name CDATA #REQUIRED>");
		out.println("<!ATTLIST dms description CDATA #REQUIRED>");
		out.println("<!ATTLIST dms lon CDATA #IMPLIED>");
		out.println("<!ATTLIST dms lat CDATA #IMPLIED>");
	}

	/** Print the body of the TMS config XML file */
	protected void printBody(PrintWriter out) {
		node_writer.print(out, meter_writer.getNodeMapping());
		cam_writer.print(out);
		printDmsBody(out);
	}

	/** Print the DMS elements */
	protected void printDmsBody(final PrintWriter out) {
		DMSHelper.find(new Checker<DMS>() {
			public boolean check(DMS dms) {
				if(dms instanceof DMSImpl)
					((DMSImpl)dms).printXml(out);
				return false;
			}
		});
	}

	/** Print the tail of the TMS config XML file */
	protected void printTail(PrintWriter out) {
		out.println("</tms_config>");
	}
}
