/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2015  Minnesota Department of Transportation
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

import java.io.InputStream;
import java.io.IOException;

/**
 * ASN1 Octet String.
 *
 * @author Douglas Lau
 */
public class ASN1OctetString extends ASN1Object {

	/** Create a new ASN1 octet string */
	public ASN1OctetString(MIBNode n, int idx) {
		super(n, new int[] { idx });
	}

	/** Create a new ASN1 octet string */
	public ASN1OctetString(MIBNode n, int idx, int j) {
		super(n, new int[] { idx, j });
	}

	/** Create a new ASN1 octet string */
	public ASN1OctetString(MIBNode n) {
		super(n, new int[] { 0 });
	}

	/** Actual octet string value */
	private byte[] value = new byte[0];

	/** Set the octet string value */
	protected void setByteValue(byte[] v) {
		value = v;
	}

	/** Get the octet string value */
	protected byte[] getByteValue() {
		return value;
	}

	/** Set the octet string value */
	public void setOctetString(byte[] v) throws IOException {
		setByteValue(v);
	}

	/** Get the octet string value */
	public byte[] getOctetString() throws IOException {
		return getByteValue();
	}

	/** Get the object value */
	@Override
	public String getValue() {
		StringBuilder b = new StringBuilder();
		for (byte v: getByteValue()) {
			b.append(v & 0xFF);
			b.append(",");
		}
		// remove trailing comma
		if (b.length() > 1)
			b.setLength(b.length() - 1);
		return b.toString();
	}

	/** Encode an octet string */
	@Override
	public void encode(BER er) throws IOException {
		er.encodeOctetString(getOctetString());
	}

	/** Decode an octet string */
	@Override
	public void decode(InputStream is, BER er) throws IOException {
		setOctetString(er.decodeOctetString(is));
	}
}
