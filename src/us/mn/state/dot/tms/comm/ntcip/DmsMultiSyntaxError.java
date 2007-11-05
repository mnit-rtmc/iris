/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2002  Minnesota Department of Transportation
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
 * Ntcip DmsMultiSyntaxError object
 *
 * @author Douglas Lau
 */
public class DmsMultiSyntaxError extends SignControl implements ASN1Integer {

	/** MULTI syntax error codes */
	static public final int UNDEFINED = 0;
	static public final int OTHER = 1;
	static public final int NONE = 2;
	static public final int UNSUPPORTED_TAG = 3;
	static public final int UNSUPPORTED_TAG_VALUE = 4;
	static public final int TEXT_TOO_BIG = 5;
	static public final int FONT_NOT_DEFINED = 6;
	static public final int CHARACTER_NOT_DEFINED = 7;
	static public final int FIELD_DEVICE_NOT_EXIST = 8;
	static public final int FIELD_DEVICE_ERROR = 9;
	static public final int FLASH_REGION_ERROR = 10;
	static public final int TAG_CONFLICT = 11;
	static public final int TOO_MANY_PAGES = 12;

	/** MULTI syntax error descriptions */
	static protected final String[] DESCRIPTION = {
		"???", "other", "none", "unsupportedTag", "unsupportedTagValue",
		"textTooBig", "fontNotDefined", "characterNotDefined",
		"fieldDeviceNotExist", "fieldDeviceError", "flashRegionError",
		"tagConflict", "tooManyPages"
	};

	/** Create a new DmsMultiSyntaxError object */
	public DmsMultiSyntaxError() {
		super(18);
	}

	/** Get the object name */
	protected String getName() { return "dmsMultiSyntaxError"; }

	/** MULTI syntax error */
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
