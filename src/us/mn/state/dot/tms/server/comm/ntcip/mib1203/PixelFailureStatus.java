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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;

/**
 * PixelFailureStatus
 *
 * @author Douglas Lau
 */
public class PixelFailureStatus extends PixelFailureTable
	implements ASN1Integer
{
	/** Stuck on (for 1203v1 stuck off assumed when unset) */
	static public final int STUCK_ON = 1 << 0;

	/** Color error */
	static public final int COLOR_ERROR = 1 << 1;

	/** Electrical error */
	static public final int ELECTRICAL_ERROR = 1 << 2;

	/** Mechanical error */
	static public final int MECHANICAL_ERROR = 1 << 3;

	/** Stuck off (added in 1203v2) */
	static public final int STUCK_OFF = 1 << 4;

	/** Partial failure (added in 1203v2) */
	static public final int PARTIAL_FAILURE = 1 << 5;

	/** Create a new pixel failure status object */
	public PixelFailureStatus(int r) {
		this(r, 1);
	}

	/** Create a new pixel failure status object */
	public PixelFailureStatus(int r, int s) {
		super(r);
		status = s;
	}

	/** Get the object name */
	protected String getName() {
		return "pixelFailureStatus";
	}

	/** Get the pixel failure table item */
	protected int getTableItem() {
		return 5;
	}

	/** Actual pixel failure status */
	protected int status;

	/** Set the integer value */
	public void setInteger(int value) {
		status = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return status;
	}

	/** Test if the pixel is stuck on */
	public boolean isStuckOn() {
		return (status & STUCK_ON) != 0;
	}

	/** Test if the pixel is stuck off */
	public boolean isStuckOff() {
		return isStuckOffV1() || isStuckOffV2();
	}

	/** Test if the pixel is stuck off (1203v1) */
	protected boolean isStuckOffV1() {
		return (status & (STUCK_ON | PARTIAL_FAILURE)) == 0;
	}

	/** Test if the pixel is stuck off (1203v2) */
	protected boolean isStuckOffV2() {
		return (status & STUCK_OFF) != 0;
	}

	/** Get the object value */
	public String getValue() {
		StringBuilder b = new StringBuilder();
		if(isStuckOn())
			b.append("Stuck ON, ");
		if(isStuckOff())
			b.append("Stuck OFF, ");
		if((status & PARTIAL_FAILURE) != 0)
			b.append("partial failure, ");
		if((status & COLOR_ERROR) != 0)
			b.append("color error, ");
		if((status & ELECTRICAL_ERROR) != 0)
			b.append("electrical error, ");
		if((status & MECHANICAL_ERROR) != 0)
			b.append("mechanical error, ");
		// remove trailing comma and space
		if(b.length() > 1)
			b.setLength(b.length() - 2);
		return b.toString();
	}
}
