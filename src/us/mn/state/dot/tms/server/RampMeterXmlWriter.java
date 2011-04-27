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

import java.io.PrintWriter;
import java.util.HashMap;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;

/**
 * This class writes out the current ramp meter configuration to an XML file.
 *
 * @author Douglas Lau
 */
public class RampMeterXmlWriter {

	/** Get a mapping of r_node names to meters */
	public HashMap<String, RampMeterImpl> getNodeMapping() {
		final HashMap<String, RampMeterImpl> m_nodes =
			new HashMap<String, RampMeterImpl>();
		RampMeterHelper.find(new Checker<RampMeter>() {
			public boolean check(RampMeter m) {
				if(m instanceof RampMeterImpl) {
					RampMeterImpl meter = (RampMeterImpl)m;
					R_NodeImpl n = meter.getR_Node();
					if(n != null) 
						m_nodes.put(n.getName(), meter);
				}
				return false;
			}
		});
		return m_nodes;
	}
}
