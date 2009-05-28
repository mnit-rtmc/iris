/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
 * PixelFailureXLocation
 *
 * @author Douglas Lau
 */
public class PixelFailureXLocation extends PixelFailureTable
	implements ASN1Integer
{
	/** Create a new pixel failure X location object */
	public PixelFailureXLocation(int r) {
		super(r);
		x = 1;
	}

	/** Get the object name */
	protected String getName() {
		return "pixelFailureXLocation";
	}

	/** Get the pixel failure table item */
	protected int getTableItem() {
		return 3;
	}

	/** Actual pixel failure X location */
	protected int x;

	/** Set the integer value */
	public void setInteger(int value) {
		x = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return x;
	}

	/** Get the object value */
	public String getValue() {
		return String.valueOf(x);
	}
}
