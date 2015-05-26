/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;

/**
 * Ntcip DmsSignAccess object
 *
 * @author Douglas Lau
 */
public class DmsSignAccess extends ASN1Integer {

	/** Other access (?) */
	static public final int OTHER = 1 << 0;

	/** Walk-in access */
	static public final int WALK_IN = 1 << 1;

	/** Back access */
	static public final int BACK = 1 << 2;

	/** Front access */
	static public final int FRONT = 1 << 3;

	/** Create a new DmsSignAccess object */
	public DmsSignAccess() {
		super(MIB1203.dmsSignAccess.node);
	}

	/** Get the object value */
	@Override
	public String getValue() {
		int v = getInteger();
		StringBuilder b = new StringBuilder();
		if ((v & FRONT) > 0)
			b.append("Front, ");
		if ((v & BACK) > 0)
			b.append("Back, ");
		if ((v & WALK_IN) > 0)
			b.append("Walk-in, ");
		if ((v & OTHER) > 0)
			b.append("Other, ");
		if (b.length() == 0)
			b.append("None");
		else {
			// remove trailing comma and space
			b.setLength(b.length() - 2);
		}
		return b.toString();
	}
}
