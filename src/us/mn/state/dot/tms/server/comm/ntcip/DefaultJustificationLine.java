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

import us.mn.state.dot.tms.MultiString;

/**
 * Ntcip DefaultJustificationLine object
 *
 * @author Douglas Lau
 */
public class DefaultJustificationLine extends MultiCfg implements ASN1Integer {

	/** Create a new DefaultJustificationLine object */
	public DefaultJustificationLine(MultiString.JustificationLine j) {
		super(6);
		justification = j;
	}

	/** Get the object name */
	protected String getName() {
		return "defaultJustificationLine";
	}

	/** Actual default line justification */
	protected MultiString.JustificationLine justification;

	/** Set the integer value */
	public void setInteger(int value) {
		justification =MultiString.JustificationLine.fromOrdinal(value);
	}

	/** Get the integer value */
	public int getInteger() {
		return justification.ordinal();
	}

	/** Get the object value */
	public String getValue() {
		return justification.toString();
	}
}
