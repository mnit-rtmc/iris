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
 * ASN1 Integer.  Base class for MIB integer objects.
 *
 * @author Douglas Lau
 */
public class ASN1Integer extends ASN1Object {

	/** Create a new ASN1 integer.
	 * @param n MIB node.
	 * @param idx Node index.
	 * @param j Table index. */
	public ASN1Integer(MIBNode n, int idx, int j) {
		super(n, new int[] { idx, j });
	}

	/** Create a new ASN1 integer.
	 * @param n MIB node.
	 * @param idx Node index. */
	public ASN1Integer(MIBNode n, int idx) {
		super(n, new int[] { idx });
	}

	/** Create a new ASN1 integer.
	 * @param n MIB node. */
	public ASN1Integer(MIBNode n) {
		super(n, new int[] { 0 });
	}

	/** Actual integer value */
	private int value;

	/** Set the integer value */
	public void setInteger(int v) {
		value = v;
	}

	/** Get the integer value */
	public int getInteger() {
		return value;
	}

	/** Get the object value */
	@Override
	public String getValue() {
		return String.valueOf(getInteger());
	}

	/** Encode an integer */
	@Override
	public void encode(BER er) throws IOException {
		er.encodeInteger(getInteger());
	}

	/** Decode an integer */
	@Override
	public void decode(InputStream is, BER er) throws IOException {
		setInteger(er.decodeInteger(is));
	}
}
