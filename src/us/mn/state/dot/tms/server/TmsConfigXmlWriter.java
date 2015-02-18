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

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.CommLinkHelper;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;

/**
 * This class writes out the TMS configuration data to an XML file.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class TmsConfigXmlWriter extends XmlWriter {

	/** TMS config XML file */
	static private final String CONFIG_XML = "_config.xml";

	/** Corridor manager */
	private final CorridorManager manager;

	/** Create a new TMS config XML writer */
	public TmsConfigXmlWriter(CorridorManager cm) {
		super(MainServer.districtId() + CONFIG_XML, true);
		manager = cm;
	}

	/** Write the TMS config XML file */
	@Override protected void write(Writer w) throws IOException {
		writeHead(w);
		writeBody(w);
		writeTail(w);
	}

	/** Write the head of the TMS config XML file */
	private void writeHead(Writer w) throws IOException {
		w.write(XML_DECLARATION);
		writeDtd(w);
		w.write("<tms_config time_stamp='" +
			TimeSteward.getDateInstance() + "'>\n");
	}

	/** Write the DTD */
	private void writeDtd(Writer w) throws IOException {
		w.write("<!DOCTYPE tms_config [\n");
		w.write("<!ELEMENT tms_config (corridor | camera | commlink | " +
			"controller | dms)*>\n");
		w.write("<!ATTLIST tms_config time_stamp CDATA #REQUIRED>\n");
		writeRNodeDtd(w);
		writeCameraDtd(w);
		writeCommLinkDtd(w);
		writeControllerDtd(w);
		writeDmsDtd(w);
		w.write("]>\n");
	}

	/** Write the DTD for r_node elements */
	private void writeRNodeDtd(Writer w) throws IOException {
		w.write("<!ELEMENT corridor (r_node)*>\n");
		w.write("<!ATTLIST corridor route CDATA #REQUIRED>\n");
		w.write("<!ATTLIST corridor dir CDATA #REQUIRED>\n");
		w.write("<!ELEMENT r_node (detector | meter)*>\n");
		w.write("<!ATTLIST r_node name CDATA #REQUIRED>\n");
		w.write("<!ATTLIST r_node n_type CDATA 'Station'>\n");
		w.write("<!ATTLIST r_node pickable CDATA 'f'>\n");
		w.write("<!ATTLIST r_node above CDATA 'f'>\n");
		w.write("<!ATTLIST r_node transition CDATA 'None'>\n");
		w.write("<!ATTLIST r_node station_id CDATA #IMPLIED>\n");
		w.write("<!ATTLIST r_node label CDATA ''>\n");
		w.write("<!ATTLIST r_node lon CDATA #REQUIRED>\n");
		w.write("<!ATTLIST r_node lat CDATA #REQUIRED>\n");
		w.write("<!ATTLIST r_node lanes CDATA '0'>\n");
		w.write("<!ATTLIST r_node attach_side CDATA 'right'>\n");
		w.write("<!ATTLIST r_node shift CDATA '0'>\n");
		w.write("<!ATTLIST r_node active CDATA 't'>\n");
		w.write("<!ATTLIST r_node abandoned CDATA 'f'>\n");
		w.write("<!ATTLIST r_node s_limit CDATA '" +
			R_NodeImpl.getDefaultSpeedLimit() +"'>\n");
		w.write("<!ATTLIST r_node forks CDATA #IMPLIED>\n");
		w.write("<!ELEMENT detector EMPTY>\n");
		w.write("<!ATTLIST detector name CDATA #REQUIRED>\n");
		w.write("<!ATTLIST detector label CDATA 'FUTURE'>\n");
		w.write("<!ATTLIST detector abandoned CDATA 'f'>\n");
		w.write("<!ATTLIST detector category CDATA ''>\n");
		w.write("<!ATTLIST detector lane CDATA '0'>\n");
		w.write("<!ATTLIST detector field CDATA '22.0'>\n");
		w.write("<!ATTLIST detector controller CDATA #IMPLIED>\n");
		w.write("<!ELEMENT meter EMPTY>\n");
		w.write("<!ATTLIST meter name CDATA #REQUIRED>\n");
		w.write("<!ATTLIST meter lon CDATA #IMPLIED>\n");
		w.write("<!ATTLIST meter lat CDATA #IMPLIED>\n");
		w.write("<!ATTLIST meter storage CDATA #REQUIRED>\n");
		w.write("<!ATTLIST meter max_wait CDATA '" +
			RampMeterImpl.DEFAULT_MAX_WAIT +"'>\n");
	}

	/** Write the DTD for camera elements */
	private void writeCameraDtd(Writer w) throws IOException {
		w.write("<!ELEMENT camera EMPTY>\n");
		w.write("<!ATTLIST camera name CDATA #REQUIRED>\n");
		w.write("<!ATTLIST camera description CDATA #REQUIRED>\n");
		w.write("<!ATTLIST camera lon CDATA #IMPLIED>\n");
		w.write("<!ATTLIST camera lat CDATA #IMPLIED>\n");
	}

	/** Write the DTD for comm link elements */
	private void writeCommLinkDtd(Writer w) throws IOException {
		w.write("<!ELEMENT commlink EMPTY>\n");
		w.write("<!ATTLIST commlink name CDATA #REQUIRED>\n");
		w.write("<!ATTLIST commlink description CDATA #REQUIRED>\n");
		w.write("<!ATTLIST commlink protocol CDATA #REQUIRED>\n");
	}

	/** Write the DTD for controlleri elements */
	private void writeControllerDtd(Writer w) throws IOException {
		w.write("<!ELEMENT controller EMPTY>\n");
		w.write("<!ATTLIST controller name CDATA #REQUIRED>\n");
		w.write("<!ATTLIST controller active CDATA #REQUIRED>\n");
		w.write("<!ATTLIST controller drop CDATA #REQUIRED>\n");
		w.write("<!ATTLIST controller commlink CDATA #IMPLIED>\n");
		w.write("<!ATTLIST controller lon CDATA #IMPLIED>\n");
		w.write("<!ATTLIST controller lat CDATA #IMPLIED>\n");
		w.write("<!ATTLIST controller location CDATA #REQUIRED>\n");
		w.write("<!ATTLIST controller cabinet CDATA #IMPLIED>\n");
		w.write("<!ATTLIST controller notes CDATA #IMPLIED>\n");
	}

	/** Write the DTD for DMS elements */
	private void writeDmsDtd(Writer w) throws IOException {
		w.write("<!ELEMENT dms EMPTY>\n");
		w.write("<!ATTLIST dms name CDATA #REQUIRED>\n");
		w.write("<!ATTLIST dms description CDATA #REQUIRED>\n");
		w.write("<!ATTLIST dms lon CDATA #IMPLIED>\n");
		w.write("<!ATTLIST dms lat CDATA #IMPLIED>\n");
		w.write("<!ATTLIST dms width_pixels CDATA #IMPLIED>\n");
		w.write("<!ATTLIST dms height_pixels CDATA #IMPLIED>\n");
	}

	/** Write the body of the TMS config XML file */
	private void writeBody(Writer w) throws IOException {
		writeRNodeBody(w);
		writeCameraBody(w);
		writeCommLinkBody(w);
		writeControllerBody(w);
		writeDmsBody(w);
	}

	/** Write the r_node elements */
	private void writeRNodeBody(Writer w) throws IOException {
		manager.writeXmlBody(w, getNodeMeterMapping());
	}

	/** Get a mapping of r_node names to meters */
	private Map<String, RampMeterImpl> getNodeMeterMapping() {
		HashMap<String, RampMeterImpl> m_nodes =
			new HashMap<String, RampMeterImpl>();
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while(it.hasNext()) {
			RampMeter m = it.next();
			if(m instanceof RampMeterImpl) {
				RampMeterImpl meter = (RampMeterImpl)m;
				R_NodeImpl n = meter.getR_Node();
				if(n != null) 
					m_nodes.put(n.getName(), meter);
			}
		}
		return m_nodes;
	}

	/** Write the camera elements */
	private void writeCameraBody(Writer w) throws IOException {
		Iterator<Camera> it = CameraHelper.iterator();
		while(it.hasNext()) {
			Camera c = it.next();
			if(c instanceof CameraImpl)
				((CameraImpl)c).writeXml(w);
		}
	}

	/** Write the comm link elements */
	private void writeCommLinkBody(Writer w) throws IOException {
		Iterator<CommLink> it = CommLinkHelper.iterator();
		while(it.hasNext()) {
			CommLink cl = it.next();
			if(cl instanceof CommLinkImpl)
				((CommLinkImpl)cl).writeXml(w);
		}
	}

	/** Write the controller elements */
	private void writeControllerBody(Writer w) throws IOException {
		Iterator<Controller> it = ControllerHelper.iterator();
		while(it.hasNext()) {
			Controller c = it.next();
			if(c instanceof ControllerImpl)
				((ControllerImpl)c).writeXml(w);
		}
	}

	/** Write the DMS elements */
	private void writeDmsBody(Writer w) throws IOException {
		Iterator<DMS> it = DMSHelper.iterator();
		while(it.hasNext()) {
			DMS dms = it.next();
			if(dms instanceof DMSImpl)
				((DMSImpl)dms).writeXml(w);
		}
	}

	/** Write the tail of the TMS config XML file */
	private void writeTail(Writer w) throws IOException {
		w.write("</tms_config>\n");
	}
}
