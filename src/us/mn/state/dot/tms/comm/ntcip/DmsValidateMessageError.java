/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.comm.ntcip;

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
	protected String getName() { return "dmsValidateMessageError"; }

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

	/** Test for a MULTI syntax error */
	public boolean isSyntaxMulti() {
		return error == DmsValidateMessageError.SYNTAX_MULTI;
	}

	/** Get the object value */
	public String getValue() { return DESCRIPTION[error]; }
}
