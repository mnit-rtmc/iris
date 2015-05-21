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

/**
 * SNMP Message Information Base (MIB) node
 *
 * @author Douglas Lau
 */
public class MIBNode {

	/** Create a root MIB node */
	static public MIBNode root(int[] n, String nm) {
		return new MIBNode(null, n, nm);
	}

	/** Parent node */
	private final MIBNode parent;

	/** Node ID */
	private final int[] nid;

	/** Node name */
	private final String name;

	/** Create a node in a MIB */
	private MIBNode(MIBNode p, int[] n, String nm) {
		parent = p;
		nid = n;
		name = nm;
	}

	/** Create a new child node */
	public MIBNode child(int[] n, String nm) {
		return new MIBNode(this, n, nm);
	}

	/** Create a new child node */
	public MIBNode child(int n, String nm) {
		return child(new int[] { n }, nm);
	}

	/** Create a new child node */
	public MIBNode child(int[] n) {
		return child(n, null);
	}

	/** Create a new child node */
	public MIBNode child(int n) {
		return child(new int[] { n });
	}

	/** Create an Object Identifier */
	public int[] createOID() {
		return fillOID(0);
	}

	/** Fill an Object Identifier */
	private int[] fillOID(int length) {
		int[] oid = createOID(length);
		int s = oid.length - length - nid.length;
		System.arraycopy(nid, 0, oid, s, nid.length);
		return oid;
	}

	/** Create an Object Identifier */
	private int[] createOID(int length) {
		if (parent == null)
			return new int[length + nid.length];
		else
			return parent.fillOID(length + nid.length);
	}

	/** Get the MIB index */
	public String getIndex() {
		StringBuilder b = new StringBuilder();
		for (int i = 1; i < nid.length; i++) {
			b.append('.');
			b.append(nid[i]);
		}
		return b.toString();
	}

	/** Get the node name */
	public String getName() {
		return name;
	}
}
