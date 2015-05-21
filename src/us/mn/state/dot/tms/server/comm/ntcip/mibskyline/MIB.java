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
package us.mn.state.dot.tms.server.comm.ntcip.mibskyline;

import us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201;
import us.mn.state.dot.tms.server.comm.snmp.MIBNode;

/**
 * MIB nodes for Skyline NTCIP signs.
 *
 * @author Douglas Lau
 */
public interface MIB {
	MIBNode skyline = MIB1201._private.child(18);
	MIBNode skylineDevices = skyline.child(2);
	MIBNode skylineDms = skylineDevices.child(3);
	MIBNode skylineDmsSignCfg = skylineDms.child(1);
	MIBNode skylineDmsStatus = skylineDms.child(9);
}
