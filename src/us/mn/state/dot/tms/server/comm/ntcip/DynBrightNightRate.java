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
package us.mn.state.dot.tms.server.comm.ntcip;

/**
 * DynBrightNightRate object
 *
 * @author Douglas Lau
 */
public class DynBrightNightRate extends SkylineDmsSignCfg implements ASN1Integer
{
	/** Create a new DynBrightNightRate object */
	public DynBrightNightRate() {
		this(0);
	}

	/** Create a new DynBrightNightRate object */
	public DynBrightNightRate(int r) {
		super(3);
		rate = r;
	}

	/** Get the object name */
	protected String getName() { return "dynBrightNightRate"; }

	/** Nighttime brightness ramping rate (tenths of a second) */
	protected int rate;

	/** Set the integer value */
	public void setInteger(int value) { rate = value; }

	/** Get the integer value */
	public int getInteger() { return rate; }

	/** Get the object value */
	public String getValue() { return String.valueOf(rate); }
}
