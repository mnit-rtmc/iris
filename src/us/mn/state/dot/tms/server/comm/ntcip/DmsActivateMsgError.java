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
 * Ntcip DmsActivateMsgError object
 *
 * @author Douglas Lau
 */
public class DmsActivateMsgError extends SignControl implements ASN1Integer {

	/** Activate message error codes */
	static public final int UNDEFINED = 0;
	static public final int OTHER = 1;
	static public final int NONE = 2;
	static public final int PRIORITY = 3;
	static public final int UNDER_VALIDATION = 4;
	static public final int MEMORY_TYPE = 5;
	static public final int MESSAGE_NUMBER = 6;
	static public final int MESSAGE_CRC = 7;
	static public final int SYNTAX_MULTI = 8;
	static public final int LOCAL_MODE = 9;

	/** Activate message error descriptions */
	static protected final String[] DESCRIPTION = {
		"???", "other", "none", "priority", "underValidation",
		"memoryType", "messageNumber", "messageCRC", "syntaxMULTI",
		"localMode"
	};

	/** Create a new DmsActivateMsgError object */
	public DmsActivateMsgError() {
		super(17);
	}

	/** Get the object name */
	protected String getName() { return "dmsActivateMsgError"; }

	/** Message activation error */
	protected int error;

	/** Set the integer value */
	public void setInteger(int value) {
		error = value;
		if(error < 0 || error >= DESCRIPTION.length)
			error = UNDEFINED;
	}

	/** Get the integer value */
	public int getInteger() { return error; }

	/** Get the object value */
	public String getValue() { return DESCRIPTION[error]; }
}
