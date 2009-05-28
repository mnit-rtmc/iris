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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;

/**
 * Ntcip DmsValidateMessageError object
 *
 * @author Douglas Lau
 */
public class DmsValidateMessageError extends DmsMessage implements ASN1Integer {

	/** Validate message error codes */
	static public final int UNDEFINED = 0;
	static public final int OTHER = 1;
	static public final int NONE = 2;
	static public final int BEACONS = 3;
	static public final int PIXEL_SERVICE = 4;
	static public final int SYNTAX_MULTI = 5;

	/** Validate message error descriptions */
	static protected final String[] DESCRIPTION = {
		"???", "other", "none", "beacons",
		"pixelService", "syntaxMULTI"
	};

	/** Create a new DmsValidateMessageError object */
	public DmsValidateMessageError() {
		super(2);
		oid[node++] = 9;
		oid[node++] = 0;
	}

	/** Get the object name */
	protected String getName() {
		return "dmsValidateMessageError";
	}

	/** Message activation error */
	protected int error;

	/** Set the integer value */
	public void setInteger(int value) {
		if(value < 0 || value >= DESCRIPTION.length)
			error = UNDEFINED;
		else
			error = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return error;
	}

	/** Test for a MULTI syntax error */
	public boolean isSyntaxMulti() {
		return error == SYNTAX_MULTI;
	}

	/** Get the object value */
	public String getValue() {
		return DESCRIPTION[error];
	}

	/** Test if there is an error */
	public boolean isError() {
		return error != NONE;
	}
}
