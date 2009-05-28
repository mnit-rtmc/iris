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
 * PixelFailureIndex
 *
 * @author Douglas Lau
 */
public class PixelFailureIndex extends PixelFailureTable implements ASN1Integer{

	/** Create a new pixel failure index object */
	public PixelFailureIndex(int r) {
		this(r, 1);
	}

	/** Create a new pixel failure index object */
	public PixelFailureIndex(int r, int i) {
		super(r);
		index = i;
	}

	/** Get the object name */
	protected String getName() {
		return "pixelFailureIndex";
	}

	/** Get the pixel failure table item */
	protected int getTableItem() {
		return 2;
	}

	/** Actual pixel failure index */
	protected int index;

	/** Set the integer value */
	public void setInteger(int value) {
		index = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return index;
	}

	/** Get the object value */
	public String getValue() {
		return String.valueOf(index);
	}
}
