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
 * Ntcip GlobalMaxModules object
 *
 * @author Douglas Lau
 */
public class GlobalMaxModules extends GlobalConfiguration
	implements ASN1Integer
{
	/** Create a new GlobalMaxModules object */
	public GlobalMaxModules() {
		super(2);
		oid[node++] = 2;
		oid[node++] = 0;
	}

	/** Get the object name */
	protected String getName() {
		return "globalMaxModules";
	}

	/** Actual max modules */
	protected int modules;

	/** Set the integer value */
	public void setInteger(int value) {
		modules = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return modules;
	}

	/** Get the object value */
	public String getValue() {
		return String.valueOf(modules);
	}
}
