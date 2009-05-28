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
 * ASN1 Integer.  Base class for MIB integer objects.
 *
 * @author Douglas Lau
 */
abstract public class ASN1Int extends MIBObject implements ASN1Integer {

	/** Actual integer value */
	protected int value;

	/** Set the integer value */
	public void setInteger(int v) {
		value = v;
	}

	/** Get the integer value */
	public int getInteger() {
		return value;
	}

	/** Get the object name */
	public final String getName() {
		String n = getClassName();
		return n.substring(0, 1).toLowerCase() + n.substring(1);
	}

	/** Get the class name (without packages) */
	protected String getClassName() {
		String name = getClass().getName();
		int i = name.lastIndexOf('.');
		if(i >= 0)
			return name.substring(i + 1);
		else
			return name;
	}

	/** Get the object value */
	protected String getValue() {
		return String.valueOf(value);
	}
}
