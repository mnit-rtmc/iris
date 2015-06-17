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

/**
 * MIB nodes for NTCIP 1203
 *
 * @author Douglas Lau
 */
public enum MIB1203 {
	dms				(MIB1201.devices, 3),
	dmsSignCfg			(dms, 1),
	  dmsSignAccess			(dmsSignCfg, 1),
	  dmsSignType			(dmsSignCfg, 2),
	  dmsSignHeight			(dmsSignCfg, 3),
	  dmsSignWidth			(dmsSignCfg, 4),
	  dmsHorizontalBorder		(dmsSignCfg, 5),
	  dmsVerticalBorder		(dmsSignCfg, 6),
	  dmsLegend			(dmsSignCfg, 7),
	  dmsBeaconType			(dmsSignCfg, 8),
	  dmsSignTechnology		(dmsSignCfg, 9),
	vmsCfg				(dms, 2),
	  vmsCharacterHeightPixels	(vmsCfg, 1),
	  vmsCharacterWidthPixels	(vmsCfg, 2),
	  vmsSignHeightPixels		(vmsCfg, 3),
	  vmsSignWidthPixels		(vmsCfg, 4),
	  vmsHorizontalPitch		(vmsCfg, 5),
	  vmsVerticalPitch		(vmsCfg, 6),
	  monochromeColor		(vmsCfg, 7),		// V2
	fontDefinition			(dms, 3),
	  numFonts			(fontDefinition, 1),
	  fontTable			(fontDefinition, 2),
	  fontEntry			(fontTable, 1),
	    fontIndex			(fontEntry, 1),
	    fontNumber			(fontEntry, 2),
	    fontName			(fontEntry, 3),
	    fontHeight			(fontEntry, 4),
	    fontCharSpacing		(fontEntry, 5),
	    fontLineSpacing		(fontEntry, 6),
	    fontVersionID		(fontEntry, 7),
	    fontStatus			(fontEntry, 8),
	  maxFontCharacters		(fontDefinition, 3),
	  characterTable		(fontDefinition, 4),
	  characterEntry		(characterTable, 1),
	    characterWidth		(characterEntry, 2),
	    characterBitmap		(characterEntry, 3),
	  fontMaxCharacterSize		(fontDefinition, 5), // V2
	multiCfg			(dms, 4),
	  defaultBackgroundColor	(multiCfg, 1),
	  defaultForegroundColor	(multiCfg, 2),
	  defaultFont			(multiCfg, 5),
	  defaultJustificationLine	(multiCfg, 6),
	  defaultJustificationPage	(multiCfg, 7),
	  defaultPageOnTime		(multiCfg, 8),
	  defaultPageOffTime		(multiCfg, 9),
	  dmsColorScheme		(multiCfg, 11),
	  defaultBackgroundRGB		(multiCfg, 12),		// V2
	  defaultForegroundRGB		(multiCfg, 13),		// V2
	  dmsSupportedMultiTags		(multiCfg, 14),		// V2
	  dmsMaxNumberPages		(multiCfg, 15),		// V2
	  dmsMaxMultiStringLength	(multiCfg, 16),		// V2
	dmsMessage			(dms, 5),
	  dmsNumPermanentMsg		(dmsMessage, 1),
	  dmsNumChangeableMsg		(dmsMessage, 2),
	  dmsMaxChangeableMsg		(dmsMessage, 3),
	  dmsFreeChangeableMemory	(dmsMessage, 4),
	  dmsNumVolatileMsg		(dmsMessage, 5),
	  dmsMaxVolatileMsg		(dmsMessage, 6),
	  dmsFreeVolatileMemory		(dmsMessage, 7),
	  dmsMessageTable		(dmsMessage, 8),
	  dmsMessageEntry		(dmsMessageTable, 1),
	    dmsMessageMemoryType	(dmsMessageEntry, 1),
	    dmsMessageMultiString	(dmsMessageEntry, 3),
	    dmsMessageCRC		(dmsMessageEntry, 5),
	    dmsMessageBeacon		(dmsMessageEntry, 6),
	    dmsMessagePixelService	(dmsMessageEntry, 7),
	    dmsMessageRunTimePriority	(dmsMessageEntry, 8),
	    dmsMessageStatus		(dmsMessageEntry, 9),
	  dmsValidateMessageError	(dmsMessage, 9),
	signControl			(dms, 6),
	  dmsControlMode		(signControl, 1),
	  dmsSWReset			(signControl, 2),
	  dmsActivateMessage		(signControl, 3),
	  dmsMessageTimeRemaining	(signControl, 4),
	  dmsMsgTableSource		(signControl, 5),
	  dmsMsgSourceMode		(signControl, 7),
	  dmsShortPowerRecoveryMessage	(signControl, 8),
	  dmsLongPowerRecoveryMessage	(signControl, 9),
	  dmsShortPowerLossTime		(signControl, 10),
	  dmsCommunicationsLossMessage	(signControl, 12),
	  dmsTimeCommLoss		(signControl, 13),
	  dmsPowerLossMessage		(signControl, 14),
	  dmsEndDurationMessage		(signControl, 15),
	  dmsMemoryMgmt			(signControl, 16),
	  dmsActivateMsgError		(signControl, 17),
	  dmsMultiSyntaxError		(signControl, 18),
	  dmsMultiSyntaxErrorPosition	(signControl, 19),
	  dmsMultiOtherErrorDescription	(signControl, 20),
	  vmsPixelServiceDuration	(signControl, 21),
	  vmsPixelServiceFrequency	(signControl, 22),
	  vmsPixelServiceTime		(signControl, 23),
	  dmsActivateErrorMsgCode	(signControl, 24),
	  dmsActivateMessageState	(signControl, 25),
	illum				(dms, 7),
	  dmsIllumControl		(illum, 1),
	  dmsIllumMaxPhotocellLevel	(illum, 2),
	  dmsIllumPhotocellLevelStatus	(illum, 3),
	  dmsIllumNumBrightLevels	(illum, 4),
	  dmsIllumBrightLevelStatus	(illum, 5),
	  dmsIllumManLevel		(illum, 6),
	  dmsIllumBrightnessValues	(illum, 7),
	  dmsIllumBrightnessValuesError	(illum, 8),
	  dmsIllumLightOutputStatus	(illum, 9),
	dmsStatus			(dms, 9),
	  dmsStatDoorOpen		(dmsStatus, 6),
	  statError			(dmsStatus, 7),
	  shortErrorStatus		(statError, 1),
	  pixelFailureTableNumRows	(statError, 2),
	  pixelFailureTable		(statError, 3),
	  pixelFailureEntry		(pixelFailureTable, 1),
	    pixelFailureDetectionType	(pixelFailureEntry, 1),
	    pixelFailureIndex		(pixelFailureEntry, 2),
	    pixelFailureXLocation	(pixelFailureEntry, 3),
	    pixelFailureYLocation	(pixelFailureEntry, 4),
	    pixelFailureStatus		(pixelFailureEntry, 5),
	  pixelTestActivation		(statError, 4),
	  lampFailureStuckOn		(statError, 5),
	  lampFailureStuckOff		(statError, 6),
	  lampTestActivation		(statError, 7),
	  fanFailures			(statError, 8),
	  fanTestActivation		(statError, 9),
	  controllerErrorStatus		(statError, 10),
	  dmsPowerFailureStatusMap	(statError, 11),	// V2
	  dmsPixelFailureTestRows	(statError, 19),	// V2
	  dmsPixelFailureMessageRows	(statError, 20),	// V2
	  dmsPowerNumRows		(statError, 12),	// V2
	  dmsPowerStatusTable		(statError, 13),	// V2
	  dmsPowerStatusEntry		(dmsPowerStatusTable, 1),
	    dmsPowerIndex		(dmsPowerStatusEntry, 1),
	    dmsPowerDescription		(dmsPowerStatusEntry, 2),
	    dmsPowerMfrStatus		(dmsPowerStatusEntry, 3),
	    dmsPowerStatus		(dmsPowerStatusEntry, 4),
	    dmsPowerVoltage		(dmsPowerStatusEntry, 5),
	    dmsPowerType		(dmsPowerStatusEntry, 6),
	  dmsLampNumRows		(statError, 23),	// V2
	  dmsLampStatusTable		(statError, 24),	// V2
	  dmsLampStatusEntry		(dmsLampStatusTable, 1),
	    dmsLampIndex		(dmsLampStatusEntry, 1),
	    dmsLampDescription		(dmsLampStatusEntry, 2),
	    dmsLampMfrStatus		(dmsLampStatusEntry, 3),
	    dmsLampStatus		(dmsLampStatusEntry, 4),
	    dmsLampPixelTop		(dmsLampStatusEntry, 5),
	    dmsLampPixelLeft		(dmsLampStatusEntry, 6),
	    dmsLampPixelBottom		(dmsLampStatusEntry, 7),
	    dmsLampPixelRight		(dmsLampStatusEntry, 8),
	  dmsLightSensorStatusMap	(statError, 28),	// V2
	  dmsLightSensorNumRows		(statError, 29),	// V2
	  dmsLightSensorStatusTable	(statError, 30),	// V2
	  dmsLightSensorStatusEntry	(dmsLightSensorStatusTable, 1),
	    dmsLightSensorIndex		(dmsLightSensorStatusEntry, 1),
	    dmsLightSensorDescription	(dmsLightSensorStatusEntry, 2),
	    dmsLightSensorCurrentReading(dmsLightSensorStatusEntry, 3),
	    dmsLightSensorStatus	(dmsLightSensorStatusEntry, 4),
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
	    dmsGraphicName		(dmsGraphicEntry, 3),
	    dmsGraphicHeight		(dmsGraphicEntry, 4),
	    dmsGraphicWidth		(dmsGraphicEntry, 5),
	    dmsGraphicType		(dmsGraphicEntry, 6),
	    dmsGraphicID		(dmsGraphicEntry, 7),
	    dmsGraphicTransparentEnabled(dmsGraphicEntry, 8),
	    dmsGraphicTransparentColor	(dmsGraphicEntry, 9),
	    dmsGraphicStatus		(dmsGraphicEntry, 10),
	  dmsGraphicBitmapTable		(graphicDefinition, 7),
	  dmsGraphicBitmapEntry		(dmsGraphicBitmapTable, 1),
	    dmsGraphicBitmapIndex	(dmsGraphicBitmapEntry, 1),
	    dmsGraphicBlockNumber	(dmsGraphicBitmapEntry, 2),
	    dmsGraphicBlockBitmap	(dmsGraphicBitmapEntry, 3);

	/** MIB node */
	public final MIBNode node;

	/** Create a node with MIB1201 parent */
	private MIB1203(MIB1201 p, int n) {
		node = p.node.child(n, toString());
	}

	/** Create a new MIB1203 node */
	private MIB1203(MIB1203 p, int n) {
		node = p.node.child(n, toString());
	}

	/** Make an integer */
	public ASN1Integer makeInt() {
		return new ASN1Integer(node);
	}

	/** Make an integer */
	public ASN1Integer makeInt(int r) {
		return new ASN1Integer(node, r);
	}

	/** Make an integer */
	public ASN1Integer makeInt(int r, int s) {
		return new ASN1Integer(node, r, s);
	}

	/** Make an integer */
	public ASN1Integer makeInt(DmsMessageMemoryType m, int n) {
		return new ASN1Integer(node, m.ordinal(), n);
	}
}
