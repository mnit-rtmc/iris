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
package us.mn.state.dot.tms.server.comm.ntcip;

/**
 * Ntcip FontName object
 *
 * @author Douglas Lau
 */
public class FontName extends FontTable implements ASN1OctetString {

	/** Create a new font name object */
	public FontName(int f) {
		this(f, "");
	}

	/** Create a new font name object */
	public FontName(int f, String n) {
		super(f);
		f_name = n;
	}

	/** Get the object name */
	protected String getName() { return "fontName"; }

	/** Get the font table item (for fontName objects) */
	protected int getTableItem() { return 3; }

	/** Actual font name */
	protected String f_name;

	/** Set the octet string value */
	public void setOctetString(byte[] value) { f_name = new String(value); }

	/** Get the octet string value */
	public byte[] getOctetString() { return f_name.getBytes(); }

	/** Get the object value */
	public String getValue() { return f_name; }
}
