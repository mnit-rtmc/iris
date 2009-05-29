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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

/**
 * Ntcip MIB node
 *
 * @author Douglas Lau
 */
class MIBNode {

	/** Parent node */
	protected final MIBNode parent;

	/** Node ID */
	protected final int[] nid;

	/** Create a node in a MIB */
	private MIBNode(MIBNode p, int[] n) {
		parent = p;
		nid = n;
	}

	/** Create a node in a MIB */
	private MIBNode(MIBNode p, int n) {
		this(p, new int[] { n });
	}

	/** Create an Object Identifier */
	public int[] createOID(int[] tail) {
		int[] oid = fillOID(tail.length);
		int s = oid.length - tail.length;
		System.arraycopy(tail, 0, oid, s, tail.length);
		return oid;
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

	static public final MIBNode nema = new MIBNode(null,
		new int[] { 1, 3, 6, 1, 4, 1, 1206 } );
	static public final MIBNode _private = new MIBNode(nema, 3);
	static public final MIBNode transportation = new MIBNode(nema, 4);
	static public final MIBNode devices = new MIBNode(transportation, 2);
	static public final MIBNode global = new MIBNode(devices, 6);
	static public final MIBNode globalConfiguration = new MIBNode(global,1);
	static public final MIBNode dms = new MIBNode(devices, 3);
	static public final MIBNode dmsSignCfg = new MIBNode(dms, 1);
	static public final MIBNode vmsCfg = new MIBNode(dms, 2);
	static public final MIBNode fontDefinition = new MIBNode(dms, 3);
	static public final MIBNode multiCfg = new MIBNode(dms, 4);
	static public final MIBNode dmsMessage = new MIBNode(dms, 5);
	static public final MIBNode signControl = new MIBNode(dms, 6);
	static public final MIBNode illum = new MIBNode(dms, 7);
	static public final MIBNode dmsStatus = new MIBNode(dms, 9);
	static public final MIBNode statError = new MIBNode(dmsStatus, 7);
	static public final MIBNode pixelFailureTable =new MIBNode(statError,3);
	static public final MIBNode statTemp = new MIBNode(dmsStatus, 9);

	static public final MIBNode ledstar = new MIBNode(_private, 16);
	static public final MIBNode ledstarDMS = new MIBNode(ledstar, 1);
	static public final MIBNode ledstarSignControl =
		new MIBNode(ledstarDMS, 1);
	static public final MIBNode ledstarDiagnostics =
		new MIBNode(ledstarDMS, 2);

	static public final MIBNode skyline = new MIBNode(_private, 18);
	static public final MIBNode skylineDevices = new MIBNode(skyline, 2);
	static public final MIBNode skylineDms = new MIBNode(skylineDevices, 3);
	static public final MIBNode skylineDmsSignCfg =
		new MIBNode(skylineDms, 1);
	static public final MIBNode skylineDmsStatus =new MIBNode(skylineDms,9);
}
