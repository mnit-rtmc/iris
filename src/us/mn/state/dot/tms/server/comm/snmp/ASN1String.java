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
 * An ASN1 String is an OCTET STRING which contains text which might be outside
 * of the ASCII range.  Each octet represents one character in the string.
 *
 * @author Douglas Lau
 */
public class ASN1String extends ASN1OctetString {

	/** Create a new ASN1 string */
	public ASN1String(MIBNode n, int idx, int j) {
		super(n, idx, j);
	}

	/** Create a new ASN1 string */
	public ASN1String(MIBNode n, int idx) {
		super(n, idx);
	}

	/** Create a new ASN1 string */
	public ASN1String(MIBNode n) {
		super(n);
	}

	/** Set the octet string to a string */
	public void setString(String v) {
		// FIXME: this should encode to a specific code page
		setByteValue(v.getBytes());
	}

	/** Get the object value */
	@Override
	public String getValue() {
		return new String(getByteValue());
	}
}
