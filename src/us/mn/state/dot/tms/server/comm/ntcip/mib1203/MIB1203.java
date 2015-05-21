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
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.MIBNode;
import static us.mn.state.dot.tms.server.comm.snmp.ObjFactory.*;

/**
 * MIB nodes for NTCIP 1203
 *
 * @author Douglas Lau
 */
public enum MIB1203 {
	dms				(MIB1201.devices, 3),
	dmsSignCfg			(dms, 1),
	vmsCfg				(dms, 2),
	  vmsCharacterHeightPixels	(vmsCfg, new int[] {1, 0}),
	  vmsCharacterWidthPixels	(vmsCfg, new int[] {2, 0}),
	  vmsSignHeightPixels		(vmsCfg, new int[] {3, 0}),
	  vmsSignWidthPixels		(vmsCfg, new int[] {4, 0}),
	  vmsHorizontalPitch		(vmsCfg, new int[] {5, 0}),
	  vmsVerticalPitch		(vmsCfg, new int[] {6, 0}),
	fontDefinition			(dms, 3),
	fontTable			(fontDefinition, 2),
	fontEntry			(fontTable, 1),
	characterTable			(fontDefinition, 4),
	characterEntry			(characterTable, 1),
	multiCfg			(dms, 4),
	dmsMessage			(dms, 5),
	dmsMessageTable			(dmsMessage, 8),
	dmsMessageEntry			(dmsMessageTable, 1),
	signControl			(dms, 6),
	  vmsPixelServiceDuration	(signControl, new int[] {21, 0}),
	  vmsPixelServiceFrequency	(signControl, new int[] {22, 0}),
	  vmsPixelServiceTime		(signControl, new int[] {23, 0}),
	illum				(dms, 7),
	dmsStatus			(dms, 9),
	statError			(dmsStatus, 7),
	pixelFailureTable		(statError, 3),
	pixelFailureEntry		(pixelFailureTable, 1),
	dmsPowerStatusTable		(statError, 13),
	dmsPowerStatusEntry		(dmsPowerStatusTable, 1),
	dmsLampStatusTable		(statError, 24),
	dmsLampStatusEntry		(dmsLampStatusTable, 1),
	dmsLightSensorStatusTable	(statError, 30),
	dmsLightSensorStatusEntry	(dmsLightSensorStatusTable, 1),
	statTemp			(dmsStatus, 9),
	graphicDefinition		(dms, 10),
	dmsGraphicTable			(graphicDefinition, 6),
	dmsGraphicEntry			(dmsGraphicTable, 1),
	dmsGraphicBitmapTable		(graphicDefinition, 7),
	dmsGraphicBitmapEntry		(dmsGraphicBitmapTable, 1);

	private final MIBNode node;
	private MIB1203(MIB1201 p, int n) {
		// FIXME: add name
		node = p.child(n);
	}
	private MIB1203(MIB1203 p, int n) {
		node = p.node.child(n, toString());
	}
	private MIB1203(MIB1203 p, int[] n) {
		node = p.node.child(n, toString());
	}

	public MIBNode child(int n) {
		// FIXME: add name
		return node.child(n);
	}
	public MIBNode child(int[] n) {
		// FIXME: add name
		return node.child(n);
	}
	public ASN1Integer makeInt() {
		return INTEGER.make(node);
	}
}
