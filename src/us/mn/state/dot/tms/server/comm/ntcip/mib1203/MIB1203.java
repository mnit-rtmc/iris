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

import us.mn.state.dot.tms.server.comm.ntcip.MIBNode;

/**
 * MIB nodes for NTCIP 1203
 *
 * @author Douglas Lau
 */
class MIB1203 extends MIBNode {

	/** Create a node in a MIB */
	protected MIB1203(MIBNode p, int[] n) {
		super(p, n);
	}

	/** Create a node in a MIB */
	protected MIB1203(MIBNode p, int n) {
		this(p, new int[] { n });
	}

	static public final MIBNode nema = new MIB1203(null,
		new int[] { 1, 3, 6, 1, 4, 1, 1206 } );
	static public final MIBNode _private = new MIB1203(nema, 3);
	static public final MIBNode transportation = new MIB1203(nema, 4);
	static public final MIBNode devices = new MIB1203(transportation, 2);
	static public final MIBNode global = new MIB1203(devices, 6);
	static public final MIBNode globalConfiguration = new MIB1203(global,1);
	static public final MIBNode globalModuleTable =
		new MIB1203(globalConfiguration, 3);
	static public final MIBNode moduleTableEntry =
		new MIB1203(globalModuleTable, 1);

	static public final MIBNode dms = new MIB1203(devices, 3);
	static public final MIBNode dmsSignCfg = new MIB1203(dms, 1);
	static public final MIBNode vmsCfg = new MIB1203(dms, 2);
	static public final MIBNode fontDefinition = new MIB1203(dms, 3);
	static public final MIBNode fontTable = new MIB1203(fontDefinition, 2);
	static public final MIBNode fontEntry = new MIB1203(fontTable, 1);
	static public final MIBNode characterTable =
		new MIB1203(fontDefinition, 4);
	static public final MIBNode characterEntry =
		new MIB1203(characterTable, 1);
	static public final MIBNode multiCfg = new MIB1203(dms, 4);
	static public final MIBNode dmsMessage = new MIB1203(dms, 5);
	static public final MIBNode dmsMessageTable = new MIB1203(dmsMessage,8);
	static public final MIBNode dmsMessageEntry =
		new MIB1203(dmsMessageTable, 1);
	static public final MIBNode signControl = new MIB1203(dms, 6);
	static public final MIBNode illum = new MIB1203(dms, 7);
	static public final MIBNode dmsStatus = new MIB1203(dms, 9);
	static public final MIBNode statError = new MIB1203(dmsStatus, 7);
	static public final MIBNode pixelFailureTable =new MIB1203(statError,3);
	static public final MIBNode pixelFailureEntry =
		new MIB1203(pixelFailureTable, 1);
	static public final MIBNode statTemp = new MIB1203(dmsStatus, 9);

	static public final MIBNode ledstar = new MIB1203(_private, 16);
	static public final MIBNode ledstarDMS = new MIB1203(ledstar, 1);
	static public final MIBNode ledstarSignControl =
		new MIB1203(ledstarDMS, 1);
	static public final MIBNode ledstarDiagnostics =
		new MIB1203(ledstarDMS, 2);

	static public final MIBNode skyline = new MIB1203(_private, 18);
	static public final MIBNode skylineDevices = new MIB1203(skyline, 2);
	static public final MIBNode skylineDms = new MIB1203(skylineDevices, 3);
	static public final MIBNode skylineDmsSignCfg =
		new MIB1203(skylineDms, 1);
	static public final MIBNode skylineDmsStatus =new MIB1203(skylineDms,9);
}
