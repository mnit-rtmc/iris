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
package us.mn.state.dot.tms.server.comm.ntcip.mib1201;

import us.mn.state.dot.tms.server.comm.snmp.MIBNode;

/**
 * MIB nodes for NTCIP 1201 (Global Object Definitions)
 *
 * @author Douglas Lau
 */
public interface MIB1201 {
	MIBNode nema = MIBNode.root( new int[] { 1, 3, 6, 1, 4, 1, 1206 } );
	MIBNode _private = nema.child(3);
	MIBNode transportation = nema.child(4);
	MIBNode devices = transportation.child(2);
	MIBNode global = devices.child(6);
	MIBNode globalConfiguration = global.child(1);
	MIBNode globalModuleTable = globalConfiguration.child(3);
	MIBNode moduleTableEntry = globalModuleTable.child(1);
	MIBNode globalMaxModules = globalConfiguration.child(new int[] {2, 0});
}
