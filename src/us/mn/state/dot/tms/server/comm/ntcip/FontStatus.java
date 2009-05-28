/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
 * Ntcip FontStatus object.  This object was added in 1203 v2.
 *
 * @author Douglas Lau
 */
public class FontStatus extends FontTable implements ASN1Integer {

	/** Font status codes */
	static public final int UNDEFINED = 0;
	static public final int NOT_USED = 1;
	static public final int MODIFYING = 2;
	static public final int CALCULATING_ID = 3;
	static public final int READY_FOR_USE = 4;
	static public final int IN_USE = 5;
	static public final int PERMANENT = 6;
	static public final int MODIFY_REQ = 7;
	static public final int READY_FOR_USE_REQ = 8;
	static public final int NOT_USED_REQ = 9;
	static public final int UNMANAGED_REQ = 10;
	static public final int UNMANAGED = 11;

	/** Status descriptions */
	static protected final String[] STATUS = {
		"???", "notUsed", "modifying", "calculatingID", "readyForUse",
		"inUse", "permanent", "modifyReq", "readyForUseReq",
		"notUsedReq", "unmanagedReq", "unmanaged"
	};

	/** Create a new FontStatus object */
	public FontStatus(int f) {
		super(f);
	}

	/** Get the object name */
	protected String getName() {
		return "fontStatus";
	}

	/** Get the font table item (for fontStatus objects) */
	protected int getTableItem() {
		return 8;
	}

	/** Actual font status */
	protected int status;

	/** Set the integer value */
	public void setInteger(int value) {
		if(value < 0 || value >= STATUS.length)
			status = UNDEFINED;
		else
			status = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return status;
	}

	/** Get the object value */
	public String getValue() {
		return STATUS[status];
	}
}
