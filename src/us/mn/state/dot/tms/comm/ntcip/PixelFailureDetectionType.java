/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.ntcip;

/**
 * PixelFailureDetectionType
 *
 * @author Douglas Lau
 */
public class PixelFailureDetectionType extends PixelFailureTable
	implements ASN1Integer
{
	/** Other detection type */
	static public final int OTHER = 1;

	/** Pixel test detection type */
	static public final int PIXEL_TEST = 2;

	/** Message display detection type */
	static public final int MESSAGE_DISPLAY = 3;

	/** Pixel failure detection type descriptions */
	static protected final String DETECTION_TYPE[] = {
		"???", "Other", "Pixel test", "Message display"
	};

	/** Create a new pixel failure detection type object */
	public PixelFailureDetectionType(int r) {
		this(r, 1);
	}

	/** Create a new pixel failure detection type object */
	public PixelFailureDetectionType(int r, int d) {
		super(r);
		dtype = d;
	}

	/** Get the object name */
	protected String getName() {
		return "pixelFailureDetectionType";
	}

	/** Get the pixel failure table item */
	protected int getTableItem() {
		return 1;
	}

	/** Actual pixel failure detection type */
	protected int dtype;

	/** Set the integer value */
	public void setInteger(int value) {
		dtype = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return dtype;
	}

	/** Get the object value */
	public String getValue() {
		return DETECTION_TYPE[dtype];
	}
}
