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
 * Ntcip DefaultPageOnTime object
 *
 * @author Douglas Lau
 */
public class DefaultPageOnTime extends MultiCfg implements ASN1Integer {

	/** Create a new DefaultPageOnTime object */
	public DefaultPageOnTime(int t) {
		super(8);
		time = t;
	}

	/** Get the object name */
	protected String getName() {
		return "defaultPageOnTime";
	}

	/** Actual default page on time */
	protected int time;

	/** Set the integer value */
	public void setInteger(int value) {
		time = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return time;
	}

	/** Get the object value */
	public String getValue() {
		return String.valueOf(time);
	}
}
