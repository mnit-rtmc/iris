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

import us.mn.state.dot.tms.server.comm.ntcip.ASN1OctetString;

/**
 * Ntcip CharacterBitmap object
 *
 * @author Douglas Lau
 */
public class CharacterBitmap extends ASN1OctetString {

	/** Font index */
	protected final int font;

	/** Character index */
	protected final int index;

	/** Create a new CharacterBitmap object */
	public CharacterBitmap(int f, int i) {
		font = f;
		index = i;
	}

	/** Create a new CharacterBitmap object */
	public CharacterBitmap(int f, int i, byte[] b) {
		font = f;
		index = i;
		value = b;
	}

	/** Get the object identifier */
	public int[] getOID() {
		return MIB1203.characterEntry.createOID(new int[] {
			3, font, index});
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < value.length; i++) {
			if(i > 0)
				b.append(",");
			b.append(value[i] & 0xFF);
		}
		return b.toString();
	}
}
