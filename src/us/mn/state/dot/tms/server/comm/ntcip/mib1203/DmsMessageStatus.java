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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;

/**
 * Ntcip DmsMessageStatus object
 *
 * @author Douglas Lau
 */
public class DmsMessageStatus extends DmsMessageTable implements ASN1Integer {

	/** Message status codes */
	static public final int UNDEFINED = 0;
	static public final int NOT_USED = 1;
	static public final int MODIFYING = 2;
	static public final int VALIDATING = 3;
	static public final int VALID = 4;
	static public final int ERROR = 5;
	static public final int MODIFY_REQ = 6;
	static public final int VALIDATE_REQ = 7;
	static public final int NOT_USED_REQ = 8;

	/** Status descriptions */
	static protected final String[] STATUS = {
		"???", "notUsed", "modifying", "validating", "valid",
		"error", "modifyReq", "validateReq", "notUsedReq"
	};

	/** Create a new DmsMessageStatus object */
	public DmsMessageStatus(int m, int n, int s) {
		super(m, n);
		setInteger(s);
	}

	/** Create a new DmsMessageStatus object */
	public DmsMessageStatus(int m, int n) {
		super(m, n);
	}

	/** Get the object name */
	protected String getName() {
		return "dmsMessageStatus";
	}

	/** Get the message table item (for dmsMessageStatus objects) */
	protected int getTableItem() {
		return 9;
	}

	/** Actual message status */
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

	/** Is the status "modifying"? */
	public boolean isModifying() {
		return status == MODIFYING;
	}

	/** Is the status "valid"? */
	public boolean isValid() {
		return status == VALID;
	}

	/** Get the object value */
	public String getValue() {
		return STATUS[status];
	}
}
