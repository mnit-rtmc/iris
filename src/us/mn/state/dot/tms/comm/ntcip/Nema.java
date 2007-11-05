/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.comm.ntcip;

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
	public int[] getOID() { return oid; }

	/** Node counter used by chained constructors to populate "oid" */
	protected int node = OID.length;

	/** Create a new Nema object
	  * @param n additional nodes in object identifier */
	protected Nema(int n) {
		oid = new int[node + n];
		System.arraycopy(OID, 0, oid, 0, node);
	}
}
