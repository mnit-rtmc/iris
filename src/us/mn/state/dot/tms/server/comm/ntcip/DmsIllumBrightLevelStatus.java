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
 * Ntcip DmsIllumBrightLevelStatus object
 *
 * @author Douglas Lau
 */
public class DmsIllumBrightLevelStatus extends Illum implements ASN1Integer {

	/** Create a new DmsIllumBrightLevelStatus object */
	public DmsIllumBrightLevelStatus() {
		super(5);
	}

	/** Get the object name */
	protected String getName() { return "dmsIllumBrightLevelStatus"; }

	/** Current brightness level */
	protected int level;

	/** Set the integer value */
	public void setInteger(int value) { level = value; }

	/** Get the integer value */
	public int getInteger() { return level; }

	/** Get the object value */
	public String getValue() { return String.valueOf(level); }
}
