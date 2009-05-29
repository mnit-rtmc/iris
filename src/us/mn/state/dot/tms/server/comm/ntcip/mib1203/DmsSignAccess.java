/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Int;

/**
 * Ntcip DmsSignAccess object
 *
 * @author Douglas Lau
 */
public class DmsSignAccess extends ASN1Int {

	/** Other access (?) */
	static public final int OTHER = 1 << 0;

	/** Walk-in access */
	static public final int WALK_IN = 1 << 1;

	/** Back access */
	static public final int BACK = 1 << 2;

	/** Front access */
	static public final int FRONT = 1 << 3;

	/** Append a string to a StringBuilder */
	static protected void appendString(StringBuilder b, String s) {
		if(b.length() > 0)
			b.append(", ");
		b.append(s);
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIBNode.dmsSignCfg.createOID(new int[] {1, 0});
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		if((value & FRONT) > 0)
			appendString(b, "Front");
		if((value & BACK) > 0)
			appendString(b, "Back");
		if((value & WALK_IN) > 0)
			appendString(b, "Walk-in");
		if((value & OTHER) > 0)
			appendString(b, "Other");
		if(b.length() == 0)
			b.append("None");
		return b.toString();
	}
}
