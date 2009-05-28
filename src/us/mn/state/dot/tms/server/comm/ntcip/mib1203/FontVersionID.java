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
 * Ntcip FontVersionID object
 *
 * @author Douglas Lau
 */
public class FontVersionID extends FontTable implements ASN1Integer {

	/** Create a new FontVersionID object */
	public FontVersionID(int f) {
		super(f);
	}

	/** Get the object name */
	protected String getName() {
		return "fontVersionID";
	}

	/** Get the font table item (for fontVersionID objects) */
	protected int getTableItem() {
		return 7;
	}

	/** Actual font version ID */
	protected int version;

	/** Set the integer value */
	public void setInteger(int value) {
		version = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return version;
	}

	/** Get the object value */
	public String getValue() {
		return String.valueOf(version);
	}
}
