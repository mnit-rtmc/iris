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
 * Ntcip DmsMessageMemoryType object
 *
 * @author Douglas Lau
 */
public class DmsMessageMemoryType extends DmsMessageTable 
	implements ASN1Integer
{
	/** Undefined memory type.
	 * Note: this is used by Ledstar for blank messages. */
	static public final int UNDEFINED = 0;

	/** Other memory type (manufacturer specific)
	 * @deprecated (by NTCIP standard) */
	static public final int OTHER = 1;

	/** Permanent memory (non-volatile and non-changeable) */
	static public final int PERMANENT = 2;

	/** Non-volatile and changeable memory */
	static public final int CHANGEABLE = 3;

	/** Volatile and changeable memory */
	static public final int VOLATILE = 4;

	/** Currently displayed message */
	static public final int CURRENT_BUFFER = 5;

	/** Scheduled message */
	static public final int SCHEDULE = 6;

	/** Blank message (added in amendment 1) */
	static public final int BLANK = 7;

	/** String descriptions of memory types */
	static public final String[] DESCRIPTION = {
		"???", "other", "permanent", "changeable", "volatile",
		"current buffer", "schedule", "blank"
	};

	/** Get a string description of a memory type */
	static public String getDescription(int m) {
		if(m < 0 || m >= DESCRIPTION.length)
			m = UNDEFINED;
		return DESCRIPTION[m];
	}

	/** Test if a message memory type is "blank" */
	static public boolean isBlank(int m) {
	 	// Ledstar blank messages are UNDEFINED in dmsMsgTableSource
		return m == BLANK || m == UNDEFINED;
	}

	/** Create a new memory type object */
	public DmsMessageMemoryType(int m, int n) {
		super(m, n);
	}

	/** Create a new memory type object */
	public DmsMessageMemoryType(int m, int n, int i) {
		super(m, n);
		memory = i;
	}

	/** Get the object name */
	protected String getName() { return "dmsMessageMemoryType"; }

	/** Get the message table item (for dmsMessageMemoryType objects) */
	protected int getTableItem() { return 1; }

	/** Actual memory type */
	protected int memory;

	/** Set the integer value */
	public void setInteger(int value) {
		memory = value;
		if(memory < 0 || memory >= DESCRIPTION.length)
			memory = UNDEFINED;
	}

	/** Get the integer value */
	public int getInteger() { return memory; }

	/** Get the object value */
	public String getValue() { return DESCRIPTION[memory]; }
}
