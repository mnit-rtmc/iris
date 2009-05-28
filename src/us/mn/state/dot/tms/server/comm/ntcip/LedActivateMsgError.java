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
 * Ledstar LedActivateMsgError object
 *
 * @author Douglas Lau
 */
public class LedActivateMsgError extends LedstarDiagnostics
	implements ASN1Integer
{
	/** Activate message error descriptions */
	static protected final String[] ERROR = {
		"Over temperature", "Bad pixel limit", "Draw error"
	};

	/** Bit masks */
	static protected final int[] BIT = { 1, 2, 4 };

	/** Create a new LedActivateMsgError object */
	public LedActivateMsgError() {
		super(12);
	}

	/** Get the object name */
	protected String getName() { return "LedActivateMsgError"; }

	/** Message activation error */
	protected int error;

	/** Set the integer value */
	public void setInteger(int value) { error = value; }

	/** Get the integer value */
	public int getInteger() { return error; }

	/** Get the object value */
	public String getValue() {
		StringBuffer value = new StringBuffer();
		for(int i = 0; i < 3; i++) {
			if((error & BIT[i]) != 0) {
				if(value.length() > 0) value.append(" / ");
				value.append(ERROR[i]);
			}
		}
		if(value.length() < 1) value.append("None");
		return value.toString();
	}
}
