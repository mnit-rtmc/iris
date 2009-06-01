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

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;

/**
 * Ntcip DmsSignTechnology object
 *
 * @author Douglas Lau
 */
public class DmsSignTechnology extends ASN1Integer {

	/** Other technology */
	static public final int OTHER = 1 << 0;

	/** LED technology */
	static public final int LED = 1 << 1;

	/** Flip disk technology */
	static public final int FLIP_DISK = 1 << 2;

	/** Fiber optic technology */
	static public final int FIBER_OPTIC = 1 << 3;

	/** Shuttered technology */
	static public final int SHUTTERED = 1 << 4;

	/** Lamp technology */
	static public final int LAMP = 1 << 5;

	/** Drum technology */
	static public final int DRUM = 1 << 6;

	/** Append a string to a StringBuilder */
	static protected void appendString(StringBuilder b, String s) {
		if(b.length() > 0)
			b.append(", ");
		b.append(s);
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIB1203.dmsSignCfg.createOID(new int[] {9, 0});
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		if((value & DRUM) > 0)
			appendString(b, "Drum");
		if((value & LAMP) > 0)
			appendString(b, "Lamp");
		if((value & SHUTTERED) > 0)
			appendString(b, "Shuttered");
		if((value & FIBER_OPTIC) > 0)
			appendString(b, "Fiber Optics");
		if((value & FLIP_DISK) > 0)
			appendString(b, "Flip Disk");
		if((value & LED) > 0)
			appendString(b, "LED");
		if((value & OTHER) > 0)
			appendString(b, "Other");
		if(b.length() == 0)
			b.append("None");
		return b.toString();
	}
}
