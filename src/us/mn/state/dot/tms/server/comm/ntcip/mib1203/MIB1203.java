/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.ntcip.MIBNode;
import us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201;

/**
 * MIB nodes for NTCIP 1203
 *
 * @author Douglas Lau
 */
class MIB1203 extends MIBNode {

	private MIB1203() {
		super(null, null);
		assert false;
	}

	static public final MIBNode dms = MIB1201.devices.create(3);
	static public final MIBNode dmsSignCfg = dms.create(1);
	static public final MIBNode vmsCfg = dms.create(2);
	static public final MIBNode fontDefinition = dms.create(3);
	static public final MIBNode fontTable = fontDefinition.create(2);
	static public final MIBNode fontEntry = fontTable.create(1);
	static public final MIBNode characterTable = fontDefinition.create(4);
	static public final MIBNode characterEntry = characterTable.create(1);
	static public final MIBNode multiCfg = dms.create(4);
	static public final MIBNode dmsMessage = dms.create(5);
	static public final MIBNode dmsMessageTable = dmsMessage.create(8);
	static public final MIBNode dmsMessageEntry = dmsMessageTable.create(1);
	static public final MIBNode signControl = dms.create(6);
	static public final MIBNode illum = dms.create(7);
	static public final MIBNode dmsStatus = dms.create(9);
	static public final MIBNode statError = dmsStatus.create(7);
	static public final MIBNode pixelFailureTable = statError.create(3);
	static public final MIBNode pixelFailureEntry =
		pixelFailureTable.create(1);
	static public final MIBNode dmsPowerStatusTable = statError.create(13);
	static public final MIBNode dmsPowerStatusEntry =
		dmsPowerStatusTable.create(1);
	static public final MIBNode dmsLampStatusTable = statError.create(24);
	static public final MIBNode dmsLampStatusEntry =
		dmsLampStatusTable.create(1);
	static public final MIBNode statTemp = dmsStatus.create(9);
	static public final MIBNode graphicDefinition = dms.create(10);
	static public final MIBNode dmsGraphicTable =
		graphicDefinition.create(6);
	static public final MIBNode dmsGraphicEntry = dmsGraphicTable.create(1);
	static public final MIBNode dmsGraphicBitmapTable =
		graphicDefinition.create(7);
	static public final MIBNode dmsGraphicBitmapEntry =
		dmsGraphicBitmapTable.create(1);
}
