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
 * FontNumber
 *
 * @author Douglas Lau
 */
public class FontNumber extends FontTable implements ASN1Integer {

	/** Create a new font number object */
	public FontNumber(int f) {
		this(f, 1);
	}

	/** Create a new font number object */
	public FontNumber(int f, int n) {
		super(f);
		number = n;
	}

	/** Get the object name */
	protected String getName() { return "fontNumber"; }

	/** Get the font table item (for fontNumber objects) */
	protected int getTableItem() { return 2; }

	/** Actual font number */
	protected int number;

	/** Set the integer value */
	public void setInteger(int value) { number = value; }

	/** Get the integer value */
	public int getInteger() { return number; }

	/** Get the object value */
	public String getValue() { return String.valueOf(number); }
}
