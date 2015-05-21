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

import java.util.Arrays;
import junit.framework.TestCase;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;

/** 
 * OID tests.
 *
 * @author Doug Lau
 */
public class OIDTest extends TestCase {

	/** constructor */
	public OIDTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {
		int[] oid;
		oid = new DmsSignAccess().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 1, 0
		}));
		oid = new DmsSignType().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 2, 0
		}));
		oid = dmsSignHeight.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 3, 0
		}));
		oid = dmsSignWidth.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 4, 0
		}));
		oid = dmsHorizontalBorder.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 5, 0
		}));
		oid = dmsVerticalBorder.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 6, 0
		}));
		oid = new DmsLegend().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 7, 0
		}));
		oid = new DmsBeaconType().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 8, 0
		}));
		oid = new DmsSignTechnology().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 9, 0
		}));

		oid = vmsCharacterHeightPixels.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 1, 0
		}));
		oid = vmsCharacterWidthPixels.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 2, 0
		}));
		oid = vmsSignHeightPixels.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 3, 0
		}));
		oid = vmsSignWidthPixels.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 4, 0
		}));
		oid = vmsHorizontalPitch.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 5, 0
		}));
		oid = vmsVerticalPitch.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 6, 0
		}));

		oid = numFonts.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 1, 0
		}));
		oid = new FontIndex(5).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 1, 5
		}));
		oid = new FontNumber(5).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 2, 5
		}));
		oid = new FontName(5).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 3, 5
		}));
		oid = new FontHeight(5).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 4, 5
		}));
		oid = new FontCharSpacing(5).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 5, 5
		}));
		oid = new FontLineSpacing(5).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 6, 5
		}));
		oid = new FontVersionID(5).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 7, 5
		}));
		oid = new FontStatus(5).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 8, 5
		}));
		oid = maxFontCharacters.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 3, 0
		}));
		oid = new CharacterWidth(5, 32).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 4, 1, 2, 5, 32
		}));
		oid = new CharacterBitmap(5, 32).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 4, 1, 3, 5, 32
		}));
		oid = fontMaxCharacterSize.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 5, 0
		}));

		oid = defaultFont.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 4, 5, 0
		}));
		oid = new DefaultJustificationLine().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 4, 6, 0
		}));
		oid = new DefaultJustificationPage().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 4, 7, 0
		}));
		oid = defaultPageOnTime.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 4, 8, 0
		}));
		oid = defaultPageOffTime.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 4, 9, 0
		}));
		oid = new DmsColorScheme().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 4, 11, 0
		}));

		oid = new DmsMessageMemoryType(DmsMessageMemoryType.Enum.currentBuffer, 1).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 1, 5, 1
		}));
		oid = new DmsMessageMultiString(DmsMessageMemoryType.Enum.currentBuffer, 1).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 3, 5, 1
		}));
		oid = new DmsMessageCRC(DmsMessageMemoryType.Enum.currentBuffer, 1).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 5, 5, 1
		}));
		oid = new DmsMessageBeacon(DmsMessageMemoryType.Enum.currentBuffer, 1).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 6, 5, 1
		}));
		oid = new DmsMessagePixelService(DmsMessageMemoryType.Enum.currentBuffer, 1).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 7, 5, 1
		}));
		oid = new DmsMessageRunTimePriority(DmsMessageMemoryType.Enum.currentBuffer, 1).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 8, 5, 1
		}));
		oid = new DmsMessageStatus(DmsMessageMemoryType.Enum.currentBuffer, 1).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 9, 5, 1
		}));
		oid = new DmsValidateMessageError().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 9, 0
		}));

		oid = new DmsSWReset().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 2, 0
		}));
		oid = new DmsActivateMessage().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 3, 0
		}));
		oid = new DmsMessageTimeRemaining().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 4, 0
		}));
		oid = new DmsMsgTableSource().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 5, 0
		}));
		oid = new DmsMsgSourceMode().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 7, 0
		}));
		oid = new DmsShortPowerRecoveryMessage().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 8, 0
		}));
		oid = new DmsLongPowerRecoveryMessage().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 9, 0
		}));
		oid = new DmsShortPowerLossTime().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 10, 0
		}));
		oid = new DmsCommunicationsLossMessage().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 12, 0
		}));
		oid = new DmsTimeCommLoss().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 13, 0
		}));
		oid = new DmsPowerLossMessage().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 14, 0
		}));
		oid = new DmsEndDurationMessage().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 15, 0
		}));
		oid = new DmsActivateMsgError().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 17, 0
		}));
		oid = new DmsMultiSyntaxError().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 18, 0
		}));
		oid = vmsPixelServiceDuration.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 21, 0
		}));
		oid = vmsPixelServiceFrequency.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 22, 0
		}));
		oid = vmsPixelServiceTime.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 23, 0
		}));

		oid = new DmsIllumControl().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 1, 0
		}));
		oid = dmsIllumMaxPhotocellLevel.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 2, 0
		}));
		oid = dmsIllumPhotocellLevelStatus.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 3, 0
		}));
		oid = dmsIllumNumBrightLevels.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 4, 0
		}));
		oid = dmsIllumBrightLevelStatus.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 5, 0
		}));
		oid = dmsIllumManLevel.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 6, 0
		}));
		oid = new DmsIllumBrightnessValues().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 7, 0
		}));
		oid = new DmsIllumBrightnessValuesError().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 8, 0
		}));
		oid = dmsIllumLightOutputStatus.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 9, 0
		}));

		oid = new DmsStatDoorOpen().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 6, 0
		}));
		oid = new ShortErrorStatus().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 1, 0
		}));
		oid = new PixelFailureTableNumRows().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 2, 0
		}));
		oid = new PixelFailureDetectionType(5).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 3, 1, 1, 2, 5
		}));
		oid = new PixelFailureIndex(
			PixelFailureDetectionType.Enum.pixelTest, 5).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 3, 1, 2, 2, 5
		}));
		oid = new PixelFailureXLocation(
			PixelFailureDetectionType.Enum.pixelTest, 5).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 3, 1, 3, 2, 5
		}));
		oid = new PixelFailureYLocation(
			PixelFailureDetectionType.Enum.pixelTest, 5).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 3, 1, 4, 2, 5
		}));
		oid = new PixelFailureStatus(
			PixelFailureDetectionType.Enum.pixelTest, 5).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 3, 1, 5, 2, 5
		}));
		oid = new PixelTestActivation().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 4, 0
		}));
		oid = new LampFailureStuckOn().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 5, 0
		}));
		oid = new LampFailureStuckOff().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 6, 0
		}));
		oid = new LampTestActivation().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 7, 0
		}));
		oid = new FanFailures().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 8, 0
		}));
		oid = new FanTestActivation().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 9, 0
		}));
		oid = new ControllerErrorStatus().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 10, 0
		}));

		oid = tempMinCtrlCabinet.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 1, 0
		}));
		oid = tempMaxCtrlCabinet.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 2, 0
		}));
		oid = tempMinAmbient.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 3, 0
		}));
		oid = tempMaxAmbient.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 4, 0
		}));
		oid = tempMinSignHousing.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 5, 0
		}));
		oid = tempMaxSignHousing.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 6, 0
		}));

		oid = dmsGraphicMaxEntries.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 1, 0
		}));
		oid = dmsGraphicNumEntries.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 2, 0
		}));
		oid = dmsGraphicMaxSize.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 3, 0
		}));
		oid = availableGraphicMemory.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 4, 0
		}));
		oid = dmsGraphicBlockSize.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 5, 0
		}));
		oid = new DmsGraphicIndex(15).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 1, 15
		}));
		oid = new DmsGraphicNumber(15).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 2, 15
		}));
		oid = new DmsGraphicName(15).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 3, 15
		}));
		oid = new DmsGraphicHeight(15).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 4, 15
		}));
		oid = new DmsGraphicWidth(15).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 5, 15
		}));
		oid = new DmsGraphicType(15).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 6, 15
		}));
		oid = new DmsGraphicID(15).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 7, 15
		}));
		oid = new DmsGraphicTransparentEnabled(15).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 8, 15
		}));
		oid = new DmsGraphicTransparentColor(15).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 9, 15
		}));
		oid = new DmsGraphicStatus(15).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 10, 15
		}));
		oid = new DmsGraphicBitmapIndex(6, 15).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 7, 1, 1, 6, 15
		}));
		oid = new DmsGraphicBlockNumber(6, 15).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 7, 1, 2, 6, 15
		}));
		oid = new DmsGraphicBlockBitmap(6, 15).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 7, 1, 3, 6, 15
		}));

for(int i: oid) System.err.print(" " + i);
System.err.println();
	}
}
