/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2019  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.utils.MultiTag;
import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;

/**
 * Bitmap of supported MULTI tags.  This object was added in 1203v2.
 *
 * @author Douglas Lau
 */
public class DmsSupportedMultiTags extends ASN1OctetString {

	/** Create a new DmsSupportedMultiTags object */
	public DmsSupportedMultiTags() {
		super(MIB1203.dmsSupportedMultiTags.node);
	}

	/** Get the bitmap array.
	 * @return Array of 4 bytes */
	private byte[] bitmapArray() {
		byte[] val = getByteValue();
		if (val.length == 4)
			return val;
		else
			return new byte[4];
	}

	/** Set the integer value (into byte[4]) */
	public void setInteger(int v) {
		byte[] val = new byte[4];
		val[0] = (byte) ((v >>  0) & 0xFF);
		val[1] = (byte) ((v >>  8) & 0xFF);
		val[2] = (byte) ((v >> 16) & 0xFF);
		val[3] = (byte) ((v >> 24) & 0xFF);
		setByteValue(val);
	}

	/** Get the integer value (from byte[4]) */
	public int getInteger() {
		int v = 0;
		byte[] val = bitmapArray();
		v |= (((int) val[0] & 0xFF) <<  0);
		v |= (((int) val[1] & 0xFF) <<  8);
		v |= (((int) val[2] & 0xFF) << 16);
		v |= (((int) val[3] & 0xFF) << 24);
		return v;
	}

	/** Get the object value */
	@Override
	public String getValue() {
		return MultiTag.asString(getInteger());
	}
}
