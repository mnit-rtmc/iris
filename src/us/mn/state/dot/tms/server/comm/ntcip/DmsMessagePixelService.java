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
 * Ntcip DmsMessagePixelService object
 *
 * @author Douglas Lau
 */
public class DmsMessagePixelService extends DmsMessageTable
	implements ASN1Integer
{
	/** Create a new DmsMessagePixelService object */
	public DmsMessagePixelService(int m, int n, int s) {
		super(m, n);
		service = s;
	}

	/** Get the object name */
	protected String getName() { return "dmsMessagePixelService"; }

	/** Get the message table item (for dmsMessagePixelService objects) */
	protected int getTableItem() { return 7; }

	/** Actual message pixel service */
	protected int service;

	/** Set the integer value */
	public void setInteger(int value) { service = value; }

	/** Get the integer value */
	public int getInteger() { return service; }

	/** Get the object value */
	public String getValue() { return String.valueOf(service); }
}
