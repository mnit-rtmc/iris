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
public class CharacterBitmap extends CharacterTable implements ASN1OctetString {

	/** Create a new CharacterBitmap object */
	public CharacterBitmap(int f, int i, byte[] b) {
		super(f, i);
		bitmap = b;
	}

	/** Get the object name */
	protected String getName() {
		return "characterBitmap";
	}

	/** Get the character table item (for characterBitmap objects) */
	protected int getTableItem() {
		return 3;
	}

	/** Actual character bitmap */
	protected byte[] bitmap;

	/** Set the octet string value */
	public void setOctetString(byte[] value) {
		bitmap = value;
	}

	/** Get the octet string value */
	public byte[] getOctetString() {
		return bitmap;
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < bitmap.length; i++) {
			if(i > 0)
				b.append(",");
			b.append(bitmap[i] & 0xFF);
		}
		return b.toString();
	}
}
