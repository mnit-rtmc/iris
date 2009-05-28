/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2009  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.ntcip.MIBObject;

/**
 * Ntcip Nema root node
 *
 * @author Douglas Lau
 */
abstract class Nema extends MIBObject {

	/** Base Dms Object Identifier */
	static private final int[] OID = { 1, 3, 6, 1, 4, 1, 1206 };

	/** Object identifier */
	protected final int[] oid;

	/** Get the object identifier */
	public int[] getOID() {
		return oid;
	}

	/** Node counter used by chained constructors to populate "oid" */
	protected int node = OID.length;

	/** Create a new Nema object
	 * @param n additional nodes in object identifier */
	protected Nema(int n) {
		oid = new int[node + n];
		System.arraycopy(OID, 0, oid, 0, node);
	}
}
