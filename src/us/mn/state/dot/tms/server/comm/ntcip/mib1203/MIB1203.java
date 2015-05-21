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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

import us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201;
import us.mn.state.dot.tms.server.comm.snmp.MIBNode;

/**
 * MIB nodes for NTCIP 1203
 *
 * @author Douglas Lau
 */
public interface MIB1203 {
	MIBNode dms = MIB1201.devices.child(3);
	MIBNode dmsSignCfg = dms.child(1);
	MIBNode vmsCfg = dms.child(2);
	MIBNode fontDefinition = dms.child(3);
	MIBNode fontTable = fontDefinition.child(2);
	MIBNode fontEntry = fontTable.child(1);
	MIBNode characterTable = fontDefinition.child(4);
	MIBNode characterEntry = characterTable.child(1);
	MIBNode multiCfg = dms.child(4);
	MIBNode dmsMessage = dms.child(5);
	MIBNode dmsMessageTable = dmsMessage.child(8);
	MIBNode dmsMessageEntry = dmsMessageTable.child(1);
	MIBNode signControl = dms.child(6);
	MIBNode illum = dms.child(7);
	MIBNode dmsStatus = dms.child(9);
	MIBNode statError = dmsStatus.child(7);
	MIBNode pixelFailureTable = statError.child(3);
	MIBNode pixelFailureEntry = pixelFailureTable.child(1);
	MIBNode dmsPowerStatusTable = statError.child(13);
	MIBNode dmsPowerStatusEntry = dmsPowerStatusTable.child(1);
	MIBNode dmsLampStatusTable = statError.child(24);
	MIBNode dmsLampStatusEntry = dmsLampStatusTable.child(1);
	MIBNode dmsLightSensorStatusTable = statError.child(30);
	MIBNode dmsLightSensorStatusEntry = dmsLightSensorStatusTable.child(1);
	MIBNode statTemp = dmsStatus.child(9);
	MIBNode graphicDefinition = dms.child(10);
	MIBNode dmsGraphicTable = graphicDefinition.child(6);
	MIBNode dmsGraphicEntry = dmsGraphicTable.child(1);
	MIBNode dmsGraphicBitmapTable = graphicDefinition.child(7);
	MIBNode dmsGraphicBitmapEntry = dmsGraphicBitmapTable.child(1);
}
