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
 * Display String
 *
 * @author Douglas Lau
 */
abstract public class DisplayString extends ASN1OctetString {

	/** Create a new display string */
	protected DisplayString(MIBNode n) {
		super(n);
	}

	/** Set the octet string to a string */
	public void setString(String v) {
		setOctetString(v.getBytes());
	}

	/** Get the object value */
	protected String getValue() {
		return new String(value);
	}
}
