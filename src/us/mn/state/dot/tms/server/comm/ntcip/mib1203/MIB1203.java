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
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
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
	  dmsSignHeight			(dmsSignCfg, 3),
	  dmsSignWidth			(dmsSignCfg, 4),
	  dmsHorizontalBorder		(dmsSignCfg, 5),
	  dmsVerticalBorder		(dmsSignCfg, 6),
	vmsCfg				(dms, 2),
	  vmsCharacterHeightPixels	(vmsCfg, 1),
	  vmsCharacterWidthPixels	(vmsCfg, 2),
	  vmsSignHeightPixels		(vmsCfg, 3),
	  vmsSignWidthPixels		(vmsCfg, 4),
	  vmsHorizontalPitch		(vmsCfg, 5),
	  vmsVerticalPitch		(vmsCfg, 6),
	fontDefinition			(dms, 3),
	  numFonts			(fontDefinition, 1),
	  fontTable			(fontDefinition, 2),
	  fontEntry			(fontTable, 1),
	  maxFontCharacters		(fontDefinition, 3),
	  characterTable		(fontDefinition, 4),
	  characterEntry		(characterTable, 1),
	  fontMaxCharacterSize		(fontDefinition, 5), // V2
	multiCfg			(dms, 4),
	  defaultFont			(multiCfg, 5),
	  defaultPageOnTime		(multiCfg, 8),
	  defaultPageOffTime		(multiCfg, 9),
	  dmsColorScheme		(multiCfg, 11),
	  dmsMaxNumberPages		(multiCfg, 15),
	  dmsMaxMultiStringLength	(multiCfg, 16),
	dmsMessage			(dms, 5),
	  dmsNumPermanentMsg		(dmsMessage, 1),
	  dmsNumChangeableMsg		(dmsMessage, 2),
	  dmsMaxChangeableMsg		(dmsMessage, 3),
	  dmsFreeChangeableMemory	(dmsMessage, 4),
	  dmsNumVolatileMsg		(dmsMessage, 5),
	  dmsMaxVolatileMsg		(dmsMessage, 6),
	  dmsFreeVolatileMemory		(dmsMessage, 7),
	dmsMessageTable			(dmsMessage, 8),
	  dmsMessageEntry		(dmsMessageTable, 1),
	signControl			(dms, 6),
	  dmsControlMode		(signControl, 1),
	  dmsSWReset			(signControl, 2),
	  dmsMessageTimeRemaining	(signControl, 4),
	  dmsMsgSourceMode		(signControl, 7),
	  dmsShortPowerLossTime		(signControl, 10),
	  dmsTimeCommLoss		(signControl, 13),
	  dmsMemoryMgmt			(signControl, 16),
	  dmsActivateMsgError		(signControl, 17),
	  dmsMultiSyntaxError		(signControl, 18),
	  dmsMultiSyntaxErrorPosition	(signControl, 19),
	  vmsPixelServiceDuration	(signControl, 21),
	  vmsPixelServiceFrequency	(signControl, 22),
	  vmsPixelServiceTime		(signControl, 23),
	  dmsActivateMessageState	(signControl, 25),
	illum				(dms, 7),
	  dmsIllumMaxPhotocellLevel	(illum, 2),
	  dmsIllumPhotocellLevelStatus	(illum, 3),
	  dmsIllumNumBrightLevels	(illum, 4),
	  dmsIllumBrightLevelStatus	(illum, 5),
	  dmsIllumManLevel		(illum, 6),
	  dmsIllumLightOutputStatus	(illum, 9),
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
	  tempMinCtrlCabinet		(statTemp, 1),
	  tempMaxCtrlCabinet		(statTemp, 2),
	  tempMinAmbient		(statTemp, 3),
	  tempMaxAmbient		(statTemp, 4),
	  tempMinSignHousing		(statTemp, 5),
	  tempMaxSignHousing		(statTemp, 6),
	graphicDefinition		(dms, 10),
	  dmsGraphicMaxEntries		(graphicDefinition, 1),
	  dmsGraphicNumEntries		(graphicDefinition, 2),
	  dmsGraphicMaxSize		(graphicDefinition, 3),
	  availableGraphicMemory	(graphicDefinition, 4),
	  dmsGraphicBlockSize		(graphicDefinition, 5),
	  dmsGraphicTable		(graphicDefinition, 6),
	  dmsGraphicEntry		(dmsGraphicTable, 1),
	    dmsGraphicIndex		(dmsGraphicEntry, 1),
	    dmsGraphicNumber		(dmsGraphicEntry, 2),
	    dmsGraphicHeight		(dmsGraphicEntry, 4),
	    dmsGraphicWidth		(dmsGraphicEntry, 5),
	    dmsGraphicType		(dmsGraphicEntry, 6),
	    dmsGraphicID		(dmsGraphicEntry, 7),
	    dmsGraphicTransparentEnabled(dmsGraphicEntry, 8),
	    dmsGraphicStatus		(dmsGraphicEntry, 10),
	  dmsGraphicBitmapTable		(graphicDefinition, 7),
	    dmsGraphicBitmapEntry	(dmsGraphicBitmapTable, 1),
	      dmsGraphicBlockNumber	(dmsGraphicBitmapEntry, 2);

	public final MIBNode node;

	private MIB1203(MIB1201 p, int n) {
		node = p.child(n);
	}
	private MIB1203(MIB1203 p, int n) {
		node = p.node.child(n, toString());
	}
	public int[] oid(int i) {
		int[] o = node.createOID(1);
		o[o.length - 1] = i;
		return o;
	}
	public int[] oid() {
		return oid(0);
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
	public ASN1Integer makeInt(int r) {
		return INTEGER.make(node, new int[] { r });
	}
	public ASN1Integer makeInt(int[] idx) {
		return INTEGER.make(node, idx);
	}
}
