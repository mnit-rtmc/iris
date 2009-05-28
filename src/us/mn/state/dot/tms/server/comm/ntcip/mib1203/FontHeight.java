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
 * Ntcip FontHeight object
 *
 * @author Douglas Lau
 */
public class FontHeight extends FontTable implements ASN1Integer {

	/** Create a new font height object */
	public FontHeight(int f) {
		this(f, 7);
	}

	/** Create a new font height object */
	public FontHeight(int f, int h) {
		super(f);
		height = h;
	}

	/** Get the object name */
	protected String getName() {
		return "fontHeight";
	}

	/** Get the font table item (for fontHeight objects) */
	protected int getTableItem() {
		return 4;
	}

	/** Actual font height */
	protected int height;

	/** Set the integer value */
	public void setInteger(int value) {
		height = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return height;
	}

	/** Get the object value */
	public String getValue() {
		return String.valueOf(height);
	}
}
