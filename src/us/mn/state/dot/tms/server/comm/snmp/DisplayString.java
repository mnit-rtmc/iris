/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.snmp;

/**
 * DisplayString as defined in RFC 1213.
 *
 * @author Douglas Lau
 */
public class DisplayString extends ASN1OctetString {

	/** Create a new display string */
	public DisplayString(MIBNode n, int idx, int j) {
		super(n, idx, j);
	}

	/** Create a new display string */
	public DisplayString(MIBNode n, int idx) {
		super(n, idx);
	}

	/** Create a new display string */
	public DisplayString(MIBNode n) {
		super(n);
	}

	/** Set string value */
	public void setString(String v) {
		// FIXME: should only accept NVT ASCII from RFC 854
		setByteValue(v.getBytes());
	}

	/** Get the object value */
	@Override
	public String getValue() {
		return new String(getByteValue());
	}
}
