/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2015  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.ColorClassic;
import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;

/**
 * Default background RGB color.  This object was added in 1203v2.
 *
 * @author Douglas Lau
 */
public class DefaultBackgroundRGB extends ASN1OctetString {

	/** Create a new DefaultBackgroundRGB object */
	public DefaultBackgroundRGB() {
		super(MIB1203.multiCfg.child(new int[] {12, 0}));
	}

	/** Get the value array.
	 * @return Array of 3 bytes (RGB color) */
	private byte[] valArray() {
		byte[] val = value;
		if(val.length == 1 || val.length == 3)
			return val;
		else
			return new byte[3];
	}

	/** Get the object value */
	public String getValue() {
		byte[] bg = valArray();
		StringBuilder sb = new StringBuilder();
		if(bg.length == 3) {
			sb.append('(');
			sb.append(bg[0] & 0xFF);
			sb.append(',');
			sb.append(bg[1] & 0xFF);
			sb.append(',');
			sb.append(bg[2] & 0xFF);
			sb.append(')');
		} else
			sb.append(ColorClassic.fromOrdinal(bg[0]));
		return sb.toString();
	}
}
