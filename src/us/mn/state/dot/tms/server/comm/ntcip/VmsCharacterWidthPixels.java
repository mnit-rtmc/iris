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
 * Ntcip VmsCharacterWidthPixels object
 *
 * @author Douglas Lau
 */
public class VmsCharacterWidthPixels extends VmsCfg implements ASN1Integer {

	/** Create a new VmsCharacterWidthPixels object */
	public VmsCharacterWidthPixels() {
		super(2);
	}

	/** Get the object name */
	protected String getName() { return "vmsCharacterWidthPixels"; }

	/** Character width (in pixels) */
	protected int width;

	/** Set the integer value */
	public void setInteger(int value) { width = value; }

	/** Get the integer value */
	public int getInteger() { return width; }

	/** Get the object value */
	public String getValue() { return String.valueOf(width); }
}
