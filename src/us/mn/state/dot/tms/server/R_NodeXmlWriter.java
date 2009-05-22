/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
	public R_NodeXmlWriter(CorridorManager m) {
		super(R_NODE_XML, false);
		manager = m;
	}

	/** Print the body of the r_node list XML file */
	public void print(PrintWriter out) {
		manager.printXmlBody(out);
	}
}
