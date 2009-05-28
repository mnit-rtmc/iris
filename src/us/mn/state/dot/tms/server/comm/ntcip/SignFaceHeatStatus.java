/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2009  Minnesota Department of Transportation
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
 * Ntcip SignFaceHeatStatus object
 *
 * @author Douglas Lau
 */
public class SignFaceHeatStatus extends SkylineDmsStatus implements ASN1Integer
{
	/** Heat off state */
	static public final int OFF = 0;

	/** Heat on state */
	static public final int ON = 1;

	/** Status strings */
	static protected final String[] STATUS = { "OFF", "ON" };

	/** Create a new SignFaceHeatStatus object */
	public SignFaceHeatStatus() {
		super(2);
		oid[node++] = 4;
		oid[node++] = 0;
	}

	/** Get the object name */
	protected String getName() { return "signFaceHeatStatus"; }

	/** Sign face heat status */
	protected int status;

	/** Set the integer value */
	public void setInteger(int value) {
		if(value < 0 || value >= STATUS.length) value = OFF;
		status = value;
	}

	/** Get the integer value */
	public int getInteger() { return status; }

	/** Get the object value */
	public String getValue() { return STATUS[status]; }
}
