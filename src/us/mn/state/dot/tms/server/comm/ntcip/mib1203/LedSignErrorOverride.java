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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import us.mn.state.dot.tms.server.comm.ntcip.ASN1Integer;

/**
 * LedSignErrorOverride object
 *
 * @author Douglas Lau
 */
public class LedSignErrorOverride extends LedstarSignControl
	implements ASN1Integer
{
	/** Override off state */
	static public final int OFF = 0;

	/** Override on state */
	static public final int ON = 1;

	/** Create a new LedSignErrorOverride object */
	public LedSignErrorOverride() {
		this(OFF);
	}

	/** Create a new LedSignErrorOverride object */
	public LedSignErrorOverride(int o) {
		super(2);
		override = o;
	}

	/** Get the object name */
	protected String getName() {
		return "LedSignErrorOverride";
	}

	/** Sign error override */
	protected int override;

	/** Set the integer value */
	public void setInteger(int value) {
		override = value;
	}

	/** Get the integer value */
	public int getInteger() {
		return override;
	}

	/** Get the object value */
	public String getValue() {
		return String.valueOf(override);
	}
}
