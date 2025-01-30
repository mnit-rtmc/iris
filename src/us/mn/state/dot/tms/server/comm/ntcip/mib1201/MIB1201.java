/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;
import us.mn.state.dot.tms.server.comm.snmp.MIBNode;

/**
 * MIB nodes for NTCIP 1201 (Global Object Definitions)
 *
 * @author Douglas Lau
 */
public enum MIB1201 {
	nema				(new int[] { 1, 3, 6, 1, 4, 1, 1206 }),
	_private			(nema, 3),
	transportation			(nema, 4),
	devices				(transportation, 2),
	global				(devices, 6),
	  globalConfiguration		(global, 1),
	    globalSetIDParameter	(globalConfiguration, 1),
	    globalMaxModules		(globalConfiguration, 2),
	    globalModuleTable		(globalConfiguration, 3),
	      moduleTableEntry		(globalModuleTable, 1),
	        moduleNumber		(moduleTableEntry, 1),
	        moduleDeviceNode	(moduleTableEntry, 2),
	        moduleMake		(moduleTableEntry, 3),
	        moduleModel		(moduleTableEntry, 4),
	        moduleVersion		(moduleTableEntry, 5),
	        moduleType		(moduleTableEntry, 6),
	    controllerBaseStandards	(globalConfiguration, 4),
	  globalDBManagement		(global, 2),
	    dbCreateTransaction		(globalDBManagement, 1),
	    dbErrorType			(globalDBManagement, 2), // deprecated
	    dbErrorID			(globalDBManagement, 3), // deprecated
	    dbTransactionID		(globalDBManagement, 4), // deprecated
	    dbMakeID			(globalDBManagement, 5), // deprecated
	    dbVerifyStatus		(globalDBManagement, 6),
	    dbVerifyError		(globalDBManagement, 7),
	  globalTimeManagement		(global, 3),
	    globalTime			(globalTimeManagement, 1),
	    globalDaylightSaving	(globalTimeManagement, 2),
	    timebase			(globalTimeManagement, 3),
	      maxTimeBaseScheduleEntries	(timebase, 1),
	      timeBaseScheduleTable		(timebase, 2),
	        timeBaseScheduleEntry		(timeBaseScheduleTable, 1),
	          timeBaseScheduleNumber	(timeBaseScheduleEntry, 1),
	          timeBaseScheduleMonth		(timeBaseScheduleEntry, 2),
	          timeBaseScheduleDay		(timeBaseScheduleEntry, 3),
	          timeBaseScheduleDate		(timeBaseScheduleEntry, 4),
	          timeBaseScheduleDayPlan	(timeBaseScheduleEntry, 5),
	      maxDayPlans			(timebase, 3),
	      maxDayPlanEvents			(timebase, 4),
	      timeBaseDayPlanTable		(timebase, 5),
	        timeBaseDayPlanEntry		(timeBaseDayPlanTable, 1),
	          dayPlanNumber			(timeBaseDayPlanEntry, 1),
	          dayPlanEventNumber		(timeBaseDayPlanEntry, 2),
	          dayPlanHour			(timeBaseDayPlanEntry, 3),
	          dayPlanMinute			(timeBaseDayPlanEntry, 4),
	          dayPlanActionNumberOID	(timeBaseDayPlanEntry, 5),
	      dayPlanStatus			(timebase, 6),
	      timeBaseScheduleTableStatus	(timebase, 7),
	  auxIOv2				(global, 7),
	    maxAuxIOv2TableNumDigitalPorts	(auxIOv2, 1),
	    maxAuxIOv2TableNumAnalogPorts	(auxIOv2, 2),
	    auxIOv2Table			(auxIOv2, 3),
	      auxIOv2Entry			(auxIOv2Table, 1),
	        auxIOv2PortType			(auxIOv2Entry, 1),
	        auxIOv2PortNumber		(auxIOv2Entry, 2),
	        auxIOv2PortDescription		(auxIOv2Entry, 3),
	        auxIOv2PortResolution		(auxIOv2Entry, 4),
	        auxIOv2PortValue		(auxIOv2Entry, 5),
	        auxIOv2PortDirection		(auxIOv2Entry, 6),
	        auxIOv2PortLastCommandedState	(auxIOv2Entry, 7);

	/** MIB node */
	public final MIBNode node;

	/** Create the root node */
	private MIB1201(int[] n) {
		node = MIBNode.root(n, toString());
	}

	/** Create a new MIB1201 node */
	private MIB1201(MIB1201 p, int n) {
		node = p.node.child(n, toString());
	}

	/** Make an integer */
	public ASN1Integer makeInt() {
		return new ASN1Integer(node);
	}

	/** Make a string */
	public ASN1String makeStr(int r) {
		return new ASN1String(node, r);
	}
}
