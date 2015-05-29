/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1201;

import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;
import us.mn.state.dot.tms.server.comm.snmp.MIBNode;

/**
 * MIB nodes for NTCIP 1201 (Global Object Definitions)
 *
 * @author Douglas Lau
 */
public enum MIB1201 {
	nema				(new int[] { 1, 3, 6, 1, 4, 1, 1206 }),
	_private			(nema, 3),
	transportation			(nema, 4),
	devices				(transportation, 2),
	global				(devices, 6),
	globalConfiguration		(global, 1),
	globalMaxModules		(globalConfiguration, 2),
	globalModuleTable		(globalConfiguration, 3),
	  moduleTableEntry		(globalModuleTable, 1),
	    moduleMake			(moduleTableEntry, 3),
	    moduleModel			(moduleTableEntry, 4),
	    moduleVersion		(moduleTableEntry, 5),
	    moduleType			(moduleTableEntry, 6);

	/** MIB node */
	public final MIBNode node;

	/** Create the root node */
	private MIB1201(int[] n) {
		node = MIBNode.root(n, toString());
	}

	/** Create a new MIB1201 node */
	private MIB1201(MIB1201 p, int n) {
		node = p.node.child(n, toString());
	}

	/** Make an integer */
	public ASN1Integer makeInt() {
		return new ASN1Integer(node);
	}

	/** Make a string */
	public ASN1String makeStr(int r) {
		return new ASN1String(node, r);
	}
}
