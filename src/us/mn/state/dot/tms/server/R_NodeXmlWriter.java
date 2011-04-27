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

/**
 * This class writes out the current r_node configuration to an XML file.
 *
 * @author Douglas Lau
 */
public class R_NodeXmlWriter extends XmlWriter {

	/** R_Node list XML file */
	static protected final String R_NODE_XML = "r_nodes.xml";

	/** Corridor manager */
	protected final CorridorManager manager;

	/** Create a new r_node XML writer */
	public R_NodeXmlWriter() {
		super(R_NODE_XML, false);
		manager = BaseObjectImpl.corridors;
	}

	/** Print the DTD for r_node elements */
	public void printDtd(PrintWriter out) {
		out.println("<!ELEMENT corridor (r_node)*>");
		out.println("<!ATTLIST corridor route CDATA #REQUIRED>");
		out.println("<!ATTLIST corridor dir CDATA #REQUIRED>");
		out.println("<!ELEMENT r_node (detector)*>");
		out.println("<!ATTLIST r_node name CDATA #REQUIRED>");
		out.println("<!ATTLIST r_node n_type CDATA 'Station'>");
		out.println("<!ATTLIST r_node pickable CDATA 'f'>");
		out.println("<!ATTLIST r_node above CDATA 'f'>");
		out.println("<!ATTLIST r_node transition CDATA 'None'>");
		out.println("<!ATTLIST r_node station_id CDATA #IMPLIED>");
		out.println("<!ATTLIST r_node label CDATA ''>");
		out.println("<!ATTLIST r_node easting CDATA '0'>");
		out.println("<!ATTLIST r_node northing CDATA '0'>");
		out.println("<!ATTLIST r_node lanes CDATA '0'>");
		out.println("<!ATTLIST r_node attach_side CDATA 'right'>");
		out.println("<!ATTLIST r_node shift CDATA '0'>");
		out.println("<!ATTLIST r_node s_limit CDATA '" +
			R_NodeImpl.DEFAULT_SPEED_LIMIT +"'>");
		out.println("<!ATTLIST r_node downstream CDATA #IMPLIED>");
		out.println("<!ELEMENT detector EMPTY>");
		out.println("<!ATTLIST detector name CDATA #REQUIRED>");
		out.println("<!ATTLIST detector label CDATA 'FUTURE'>");
		out.println("<!ATTLIST detector abandoned CDATA 'f'>");
		out.println("<!ATTLIST detector category CDATA ''>");
		out.println("<!ATTLIST detector lane CDATA '0'>");
		out.println("<!ATTLIST detector field CDATA '22.0'>");
	}

	/** Print the body of the r_node list XML file */
	public void print(PrintWriter out) {
		manager.printXmlBody(out);
	}
}
