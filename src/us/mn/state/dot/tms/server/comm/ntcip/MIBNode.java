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
 * SNMP Message Information Base (MIB) node
 *
 * @author Douglas Lau
 */
public class MIBNode {

	/** Parent node */
	protected final MIBNode parent;

	/** Node ID */
	protected final int[] nid;

	/** Create a node in a MIB */
	protected MIBNode(MIBNode p, int[] n) {
		parent = p;
		nid = n;
	}

	/** Create a new child node */
	public MIBNode create(int[] n) {
		return new MIBNode(this, n);
	}

	/** Create a new child node */
	public MIBNode create(int n) {
		return create(new int[] { n });
	}

	/** Create an Object Identifier */
	public int[] createOID() {
		return fillOID(0);
	}

	/** Fill an Object Identifier */
	protected int[] fillOID(int length) {
		int[] oid = createOID(length);
		int s = oid.length - length - nid.length;
		System.arraycopy(nid, 0, oid, s, nid.length);
		return oid;
	}

	/** Create an Object Identifier */
	protected int[] createOID(int length) {
		if(parent == null)
			return new int[length + nid.length];
		else
			return parent.fillOID(length + nid.length);
	}

	/** Get the MIB index */
	public String getIndex() {
		StringBuilder b = new StringBuilder();
		for(int i = 1; i < nid.length; i++) {
			b.append('.');
			b.append(nid[i]);
		}
		return b.toString();
	}
}
