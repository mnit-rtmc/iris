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
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.MIBNode;

/**
 * MIB nodes for Ledstar NTCIP signs.
 *
 * ledBadPixelLimit is the number of failed pixels needed before the sign will
 * refuse to activate a message (with dmsActivateMsgError.OTHER).  Setting to
 * zero disables shortErrorStatus.PIXEL error reporting.
 *
 * @author Douglas Lau
 */
public enum MIB {
	ledstar				(MIB1201._private, 16),
	ledstarDMS			(ledstar, 1),
	ledstarSignControl		(ledstarDMS, 1),
	  ledHighTempCutoff		(ledstarSignControl, 1),
	  ledSignErrorOverride		(ledstarSignControl, 2),
	  ledBadPixelLimit		(ledstarSignControl, 3),
	  ledLdcPotBase			(ledstarSignControl, 6),
	  ledPixelLow			(ledstarSignControl, 7),
	  ledPixelHigh			(ledstarSignControl, 8),
	ledstarDiagnostics		(ledstarDMS, 2),
	  ledActivateMsgError		(ledstarDiagnostics, 12);

	/** MIB node */
	public final MIBNode node;

	/** Create a node with MIB1201 parent */
	private MIB(MIB1201 p, int n) {
		node = p.node.child(n, toString());
	}

	/** Create a new ledstar MIB node */
	private MIB(MIB p, int n) {
		node = p.node.child(n, toString());
	}

	/** Make an integer */
	public ASN1Integer makeInt() {
		return new ASN1Integer(node);
	}
}
