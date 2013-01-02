/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2013  Minnesota Department of Transportation
 * Copyright (C) 2011  Berkeley Transportation Systems Inc.
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

import java.io.PrintWriter;
import java.util.Iterator;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;

/**
 * This class writes out the TMS configuration data to an XML file.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class TmsConfigXmlWriter extends XmlWriter {

	/** TMS config XML file */
	static private final String CONFIG_XML = "_config.xml";

	/** R_Node XML writer */
	private final R_NodeXmlWriter node_writer = new R_NodeXmlWriter();

	/** Ramp meter XML writer */
	private final RampMeterXmlWriter meter_writer =
		new RampMeterXmlWriter();

	/** Camera XML writer */
	private final CameraXmlWriter cam_writer = new CameraXmlWriter();

	/** Controller XML writer */
	private final ControllerXmlWriter controller_writer =
		new ControllerXmlWriter();

	/** Commlink XML writer */
	private final CommLinkXmlWriter commlink_writer = new CommLinkXmlWriter();

	/** Create a new TMS config XML writer */
	public TmsConfigXmlWriter() {
		super(MainServer.districtId() + CONFIG_XML, true);
	}

	/** Print the TMS config XML file */
	public void print(final PrintWriter out) {
		printHead(out);
		printBody(out);
		printTail(out);
	}

	/** Print the head of the TMS config XML file */
	private void printHead(PrintWriter out) {
		out.println(XML_DECLARATION);
		printDtd(out);
		out.println("<tms_config time_stamp='" +
			TimeSteward.getDateInstance() + "'>");
	}

	/** Print the DTD */
	private void printDtd(PrintWriter out) {
		out.println("<!DOCTYPE tms_config [");
		out.println("<!ELEMENT tms_config (corridor | camera | commlink | " +
			"controller | dms)*>");
		out.println("<!ATTLIST tms_config time_stamp CDATA #REQUIRED>");
		node_writer.printDtd(out);
		printCameraDtd(out);
		printCommLinkDtd(out);
		printControllerDtd(out);
		printDmsDtd(out);
		out.println("]>");
	}

	/** Print the DTD for camera elements */
	private void printCameraDtd(PrintWriter out) {
		out.println("<!ELEMENT camera EMPTY>");
		out.println("<!ATTLIST camera name CDATA #REQUIRED>");
		out.println("<!ATTLIST camera description CDATA #REQUIRED>");
		out.println("<!ATTLIST camera lon CDATA #IMPLIED>");
		out.println("<!ATTLIST camera lat CDATA #IMPLIED>");
	}

	/** Print the DTD for comm link elements */
	private void printCommLinkDtd(PrintWriter out) {
		out.println("<!ELEMENT commlink EMPTY>");
		out.println("<!ATTLIST commlink name CDATA #REQUIRED>");
		out.println("<!ATTLIST commlink description CDATA #REQUIRED>");
		out.println("<!ATTLIST commlink protocol CDATA #REQUIRED>");
	}

	/** Print the DTD for controlleri elements */
	private void printControllerDtd(PrintWriter out) {
		out.println("<!ELEMENT controller EMPTY>");
		out.println("<!ATTLIST controller name CDATA #REQUIRED>");
		out.println("<!ATTLIST controller active CDATA #REQUIRED>");
		out.println("<!ATTLIST controller drop CDATA #REQUIRED>");
		out.println("<!ATTLIST controller commlink CDATA #IMPLIED>");
		out.println("<!ATTLIST controller lon CDATA #IMPLIED>");
		out.println("<!ATTLIST controller lat CDATA #IMPLIED>");
		out.println("<!ATTLIST controller location CDATA #REQUIRED>");
		out.println("<!ATTLIST controller cabinet CDATA #IMPLIED>");
		out.println("<!ATTLIST controller notes CDATA #IMPLIED>");
	}

	/** Print the DTD for DMS elements */
	private void printDmsDtd(PrintWriter out) {
		out.println("<!ELEMENT dms EMPTY>");
		out.println("<!ATTLIST dms name CDATA #REQUIRED>");
		out.println("<!ATTLIST dms description CDATA #REQUIRED>");
		out.println("<!ATTLIST dms lon CDATA #IMPLIED>");
		out.println("<!ATTLIST dms lat CDATA #IMPLIED>");
		out.println("<!ATTLIST dms width_pixels CDATA #IMPLIED>");
		out.println("<!ATTLIST dms height_pixels CDATA #IMPLIED>");
	}

	/** Print the body of the TMS config XML file */
	private void printBody(PrintWriter out) {
		node_writer.print(out, meter_writer.getNodeMapping());
		cam_writer.print(out);
		commlink_writer.print(out);
		controller_writer.print(out);
		printDmsBody(out);
	}

	/** Print the DMS elements */
	private void printDmsBody(final PrintWriter out) {
		Iterator<DMS> it = DMSHelper.iterator();
		while(it.hasNext()) {
			DMS dms = it.next();
			if(dms instanceof DMSImpl)
				((DMSImpl)dms).printXml(out);
		}
	}

	/** Print the tail of the TMS config XML file */
	private void printTail(PrintWriter out) {
		out.println("</tms_config>");
	}
}
