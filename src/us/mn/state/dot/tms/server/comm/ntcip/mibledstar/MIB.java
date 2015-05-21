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
package us.mn.state.dot.tms.server.comm.ntcip.mibledstar;

import us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201;
import us.mn.state.dot.tms.server.comm.snmp.MIBNode;

/**
 * MIB nodes for Ledstar NTCIP signs.
 *
 * @author Douglas Lau
 */
public enum MIB {
	ledstar			(MIB1201._private, 16),
	ledstarDMS		(ledstar, 1),
	ledstarSignControl	(ledstarDMS, 1),
	ledstarDiagnostics	(ledstarDMS, 2);

	private final MIBNode node;
	private MIB(MIB1201 p, int n) {
		// FIXME: add name
		node = p.child(n);
	}
	private MIB(MIB p, int n) {
		node = p.node.child(n, toString());
	}
	private MIB(MIB p, int[] n) {
		node = p.node.child(n, toString());
	}

	public MIBNode child(int n) {
		// FIXME: add name
		return node.child(n);
	}
	public MIBNode child(int[] n) {
		// FIXME: add name
		return node.child(n);
	}
}
