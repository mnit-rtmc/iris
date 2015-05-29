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
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * ASN1 object.  Base class for ASN1 objects.
 * FIXME: convert to use ControllerProperty encode/decode methods.
 *
 * @author Douglas Lau
 */
abstract public class ASN1Object extends ControllerProperty {

	/** MIB node */
	private final MIBNode node;

	/** Node index ID */
	private final int[] nid;

	/** Create a new ASN1 object */
	protected ASN1Object(MIBNode n, int[] nid) {
		node = n;
		this.nid = nid;
	}

	/** Get the object identifier */
	public int[] oid() {
		int[] oid = node.createOID(nid.length);
		int s = oid.length - nid.length;
		System.arraycopy(nid, 0, oid, s, nid.length);
		return oid;
	}

	/** Get the object name */
	public final String getName() {
		return node.getName();
	}

	/** Get the MIB index */
	private String getIndex() {
		StringBuilder b = new StringBuilder();
		for (int n: nid) {
			b.append('.');
			b.append(n);
		}
		return b.toString();
	}

	/** Get the object value */
	abstract public String getValue();

	/** Create an object description */
	@Override
	public String toString() {
		return getName() + getIndex() + ": " + getValue();
	}

	/** Encode the object */
	abstract public void encode(BER er) throws IOException;

	/** Decode the object */
	abstract public void decode(InputStream is, BER er) throws IOException;
}
