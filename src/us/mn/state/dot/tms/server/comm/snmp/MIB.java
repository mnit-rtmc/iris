/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.snmp;

/**
 * SNMP-2 MIB definition.
 *
 * @author Douglas Lau
 */
public enum MIB {
	system				(new int[] {1, 3, 6, 1, 2, 1, 1}),
	  sysDescr			(system, 1),
	  sysObjectID			(system, 2),
	  sysUpTime			(system, 3),
	  sysContact			(system, 4),
	  sysName			(system, 5),
	  sysLocation			(system, 6);

	/** MIB node */
	public final MIBNode node;

	/** Create a root node */
	private MIB(int[] n) {
		node = MIBNode.root(n, toString());
	}

	/** Create a new MIB node */
	private MIB(MIB p, int n) {
		node = p.node.child(n, toString());
	}

	/** Make an integer */
	public ASN1Integer makeInt() {
		return new ASN1Integer(node);
	}
}
