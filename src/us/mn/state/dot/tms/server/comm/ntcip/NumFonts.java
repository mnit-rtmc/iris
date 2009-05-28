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
 * Ntcip NumFonts object
 *
 * @author Douglas Lau
 */
public class NumFonts extends FontDefinition implements ASN1Integer {

	/** Create a new num fonts object */
	public NumFonts() {
		super(2);
		oid[node++] = 1;
		oid[node] = 0;
	}

	/** Get the object name */
	protected String getName() {
		return "numFonts";
	}

	/** Number of fonts */
	protected int num;

	/** Set the integer value */
	public void setInteger(int value) {
		num = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return num;
	}

	/** Get the object value */
	public String getValue() {
		return String.valueOf(num);
	}
}
