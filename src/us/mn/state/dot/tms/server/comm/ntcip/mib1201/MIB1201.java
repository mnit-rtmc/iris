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
package us.mn.state.dot.tms.server.comm.ntcip.mib1201;

import us.mn.state.dot.tms.server.comm.ntcip.MIBNode;

/**
 * MIB nodes for NTCIP 1201 (Global Object Definitions)
 *
 * @author Douglas Lau
 */
public class MIB1201 extends MIBNode {

	/** Create a node in a MIB */
	protected MIB1201(int[] n) {
		super(null, n);
	}

	static public final MIBNode nema = new MIB1201(
		new int[] { 1, 3, 6, 1, 4, 1, 1206 } );
	static public final MIBNode _private = nema.create(3);
	static public final MIBNode transportation = nema.create(4);
	static public final MIBNode devices = transportation.create(2);
	static public final MIBNode global = devices.create(6);
	static public final MIBNode globalConfiguration = global.create(1);
	static public final MIBNode globalModuleTable =
		globalConfiguration.create(3);
	static public final MIBNode moduleTableEntry =
		globalModuleTable.create(1);
}
