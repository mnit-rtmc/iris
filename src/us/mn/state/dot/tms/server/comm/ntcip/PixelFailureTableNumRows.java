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
 * Ntcip PixelFailureTableNumRows object
 *
 * @author Douglas Lau
 */
public class PixelFailureTableNumRows extends StatError implements ASN1Integer {

	/** Create a new PixelFailureTableNumRows object */
	public PixelFailureTableNumRows() {
		super(2);
		oid[node++] = 2;
		oid[node++] = 0;
	}

	/** Get the object name */
	protected String getName() { return "pixelFailureTableNumRows"; }

	/** Number of rows in the pixel failure table */
	protected int rows;

	/** Set the integer value */
	public void setInteger(int value) { rows = value; }

	/** Get the integer value */
	public int getInteger() { return rows; }

	/** Get the object value */
	public String getValue() { return String.valueOf(rows); }
}
