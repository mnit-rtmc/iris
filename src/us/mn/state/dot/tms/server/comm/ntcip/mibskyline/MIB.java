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
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.MIBNode;

/**
 * MIB nodes for Skyline NTCIP signs.
 *
 * @author Douglas Lau
 */
public enum MIB {
	skyline				(MIB1201._private, 18),
	skylineDevices			(skyline, 2),
	skylineDms			(skylineDevices, 3),
	skylineDmsSignCfg		(skylineDms, 1),
	  dynBrightDayNight		(skylineDmsSignCfg, 1),
	  dynBrightDayRate		(skylineDmsSignCfg, 2),
	  dynBrightNightRate		(skylineDmsSignCfg, 3),
	  dynBrightMaxNightManLvl	(skylineDmsSignCfg, 8),
	  dmsTempCritical		(skylineDmsSignCfg, 11),
	skylineDmsStatus		(skylineDms, 9),
	  illumPowerStatus		(skylineDmsStatus, 2),
	  signFaceHeatStatus		(skylineDmsStatus, 4),
	  sensorFailures		(skylineDmsStatus, 17);

	/** MIB node */
	public final MIBNode node;

	/** Create a node with MIB1201 parent */
	private MIB(MIB1201 p, int n) {
		node = p.node.child(n, toString());
	}

	/** Create a new skyline MIB node */
	private MIB(MIB p, int n) {
		node = p.node.child(n, toString());
	}

	/** Make an integer */
	public ASN1Integer makeInt() {
		return new ASN1Integer(node);
	}
}
