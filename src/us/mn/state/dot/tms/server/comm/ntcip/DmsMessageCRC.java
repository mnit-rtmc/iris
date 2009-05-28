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
 * Ntcip DmsMessageCRC object
 *
 * @author Douglas Lau
 */
public class DmsMessageCRC extends DmsMessageTable implements ASN1Integer {

	/** Create a new DmsMessageCRC object */
	public DmsMessageCRC(int m, int n) {
		super(m, n);
	}

	/** Get the object name */
	protected String getName() { return "dmsMessageCRC"; }

	/** Get the message table item (for dmsMessageCRC objects) */
	protected int getTableItem() { return 5; }

	/** Actual message CRC */
	protected int crc;

	/** Set the integer value */
	public void setInteger(int value) { crc = value; }

	/** Get the integer value */
	public int getInteger() { return crc; }

	/** Get the object value */
	public String getValue() { return String.valueOf(crc); }
}
