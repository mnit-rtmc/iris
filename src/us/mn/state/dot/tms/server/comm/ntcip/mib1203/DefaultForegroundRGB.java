/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.ntcip.ASN1OctetString;

/**
 * Default foreground RGB color.  This object was added in 1203v2.
 *
 * @author Douglas Lau
 */
public class DefaultForegroundRGB extends ASN1OctetString {

	/** Create a new DefaultForegroundRGB object */
	public DefaultForegroundRGB() {
		super(MIB1203.multiCfg.create(new int[] {13, 0}));
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
		byte[] fg = valArray();
		StringBuilder sb = new StringBuilder();
		if(fg.length == 3) {
			sb.append('(');
			sb.append(fg[0] & 0xFF);
			sb.append(',');
			sb.append(fg[1] & 0xFF);
			sb.append(',');
			sb.append(fg[2] & 0xFF);
			sb.append(')');
		} else
			sb.append(ColorClassic.fromOrdinal(fg[0]));
		return sb.toString();
	}
}
