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
 * ASN1 Octet String.
 *
 * @author Douglas Lau
 */
abstract public class ASN1OctetString extends ASN1Object {

	/** Create a new ASN1 octet string */
	protected ASN1OctetString(MIBNode n) {
		super(n);
	}

	/** Actual octet string value */
	protected byte[] value = new byte[0];

	/** Set the octet string value */
	public void setOctetString(byte[] v) {
		value = v;
	}

	/** Get the octet string value */
	public byte[] getOctetString() {
		return value;
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
