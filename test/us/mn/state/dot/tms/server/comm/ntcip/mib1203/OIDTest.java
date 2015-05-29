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

	public OIDTest(String name) {
		super(name);
	}

	public void testConfig() {
		int[] oid;
		oid = dmsSignAccess.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 1, 0
		}));
		oid = dmsSignType.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 2, 0
		}));
		oid = dmsSignHeight.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 3, 0
		}));
		oid = dmsSignWidth.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 4, 0
		}));
		oid = dmsHorizontalBorder.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 5, 0
		}));
		oid = dmsVerticalBorder.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 6, 0
		}));
		oid = dmsLegend.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 7, 0
		}));
		oid = dmsBeaconType.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 8, 0
		}));
		oid = dmsSignTechnology.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 1, 9, 0
		}));
	}

	public void testDimensions() {
		int[] oid;
		oid = vmsCharacterHeightPixels.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 1, 0
		}));
		oid = vmsCharacterWidthPixels.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 2, 0
		}));
		oid = vmsSignHeightPixels.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 3, 0
		}));
		oid = vmsSignWidthPixels.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 4, 0
		}));
		oid = vmsHorizontalPitch.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 5, 0
		}));
		oid = vmsVerticalPitch.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 6, 0
		}));
	}

	public void testFonts() {
		int[] oid;
		oid = numFonts.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 1, 0
		}));
		oid = fontIndex.node.oid(5);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 1, 5
		}));
		oid = fontNumber.node.oid(5);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 2, 5
		}));
		oid = fontName.node.oid(5);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 3, 5
		}));
		oid = fontHeight.node.oid(5);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 4, 5
		}));
		oid = fontCharSpacing.node.oid(5);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 5, 5
		}));
		oid = fontLineSpacing.node.oid(5);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 6, 5
		}));
		oid = fontVersionID.node.oid(5);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 7, 5
		}));
		oid = fontStatus.node.oid(5);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 2, 1, 8, 5
		}));
		oid = maxFontCharacters.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 3, 0
		}));
		oid = characterWidth.node.oid(5, 32);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 4, 1, 2, 5, 32
		}));
		oid = characterBitmap.node.oid(5, 32);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 4, 1, 3, 5, 32
		}));
		oid = fontMaxCharacterSize.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 3, 5, 0
		}));
	}

	public void testMultiCfg() {
		int[] oid;
		oid = defaultFont.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 4, 5, 0
		}));
		oid = defaultJustificationLine.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 4, 6, 0
		}));
		oid = defaultJustificationPage.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 4, 7, 0
		}));
		oid = defaultPageOnTime.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 4, 8, 0
		}));
		oid = defaultPageOffTime.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 4, 9, 0
		}));
		oid = dmsColorScheme.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 4, 11, 0
		}));
	}

	public void testMessage() {
		int[] oid;
		oid = dmsMessageMemoryType.node.oid(
			DmsMessageMemoryType.currentBuffer.ordinal(), 1);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 1, 5, 1
		}));
		oid = dmsMessageMultiString.node.oid(
			DmsMessageMemoryType.currentBuffer.ordinal(), 1);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 3, 5, 1
		}));
		oid = dmsMessageCRC.node.oid(
			DmsMessageMemoryType.currentBuffer.ordinal(), 1);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 5, 5, 1
		}));
		oid = dmsMessageBeacon.node.oid(
			DmsMessageMemoryType.currentBuffer.ordinal(), 1);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 6, 5, 1
		}));
		oid = dmsMessagePixelService.node.oid(
			DmsMessageMemoryType.currentBuffer.ordinal(), 1);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 7, 5, 1
		}));
		oid = dmsMessageRunTimePriority.node.oid(
			DmsMessageMemoryType.currentBuffer.ordinal(), 1);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 8, 5, 1
		}));
		oid = dmsMessageStatus.node.oid(
			DmsMessageMemoryType.currentBuffer.ordinal(), 1);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 8, 1, 9, 5, 1
		}));
		oid = dmsValidateMessageError.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 5, 9, 0
		}));
	}

	public void testControl() {
		int[] oid;
		oid = dmsSWReset.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 2, 0
		}));
		oid = dmsActivateMessage.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 3, 0
		}));
		oid = dmsMessageTimeRemaining.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 4, 0
		}));
		oid = dmsMsgTableSource.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 5, 0
		}));
		oid = dmsMsgSourceMode.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 7, 0
		}));
		oid = dmsShortPowerRecoveryMessage.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 8, 0
		}));
		oid = dmsLongPowerRecoveryMessage.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 9, 0
		}));
		oid = dmsShortPowerLossTime.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 10, 0
		}));
		oid = dmsCommunicationsLossMessage.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 12, 0
		}));
		oid = dmsTimeCommLoss.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 13, 0
		}));
		oid = dmsPowerLossMessage.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 14, 0
		}));
		oid = dmsEndDurationMessage.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 15, 0
		}));
		oid = dmsActivateMsgError.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 17, 0
		}));
		oid = dmsMultiSyntaxError.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 18, 0
		}));
		oid = vmsPixelServiceDuration.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 21, 0
		}));
		oid = vmsPixelServiceFrequency.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 22, 0
		}));
		oid = vmsPixelServiceTime.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 23, 0
		}));
	}

	public void testIllum() {
		int[] oid;
		oid = dmsIllumControl.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 1, 0
		}));
		oid = dmsIllumMaxPhotocellLevel.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 2, 0
		}));
		oid = dmsIllumPhotocellLevelStatus.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 3, 0
		}));
		oid = dmsIllumNumBrightLevels.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 4, 0
		}));
		oid = dmsIllumBrightLevelStatus.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 5, 0
		}));
		oid = dmsIllumManLevel.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 6, 0
		}));
		oid = dmsIllumBrightnessValues.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 7, 0
		}));
		oid = dmsIllumBrightnessValuesError.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 8, 0
		}));
		oid = dmsIllumLightOutputStatus.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 7, 9, 0
		}));
	}

	public void testStatus() {
		int[] oid;
		oid = dmsStatDoorOpen.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 6, 0
		}));
		oid = shortErrorStatus.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 1, 0
		}));
		oid = pixelFailureTableNumRows.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 2, 0
		}));
		int pt = PixelFailureDetectionType.pixelTest.ordinal();
		oid = pixelFailureDetectionType.node.oid(pt, 5);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 3, 1, 1, 2, 5
		}));
		oid = pixelFailureIndex.node.oid(pt, 5);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 3, 1, 2, 2, 5
		}));
		oid = pixelFailureXLocation.node.oid(pt, 5);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 3, 1, 3, 2, 5
		}));
		oid = pixelFailureYLocation.node.oid(pt, 5);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 3, 1, 4, 2, 5
		}));
		oid = pixelFailureStatus.node.oid(pt, 5);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 3, 1, 5, 2, 5
		}));
		oid = pixelTestActivation.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 4, 0
		}));
		oid = lampFailureStuckOn.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 5, 0
		}));
		oid = lampFailureStuckOff.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 6, 0
		}));
		oid = lampTestActivation.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 7, 0
		}));
		oid = fanFailures.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 8, 0
		}));
		oid = fanTestActivation.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 9, 0
		}));
		oid = controllerErrorStatus.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 7, 10, 0
		}));
	}

	public void testTemp() {
		int[] oid;
		oid = tempMinCtrlCabinet.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 1, 0
		}));
		oid = tempMaxCtrlCabinet.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 2, 0
		}));
		oid = tempMinAmbient.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 3, 0
		}));
		oid = tempMaxAmbient.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 4, 0
		}));
		oid = tempMinSignHousing.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 5, 0
		}));
		oid = tempMaxSignHousing.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 6, 0
		}));
	}

	public void testGraphic() {
		int[] oid;
		oid = dmsGraphicMaxEntries.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 1, 0
		}));
		oid = dmsGraphicNumEntries.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 2, 0
		}));
		oid = dmsGraphicMaxSize.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 3, 0
		}));
		oid = availableGraphicMemory.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 4, 0
		}));
		oid = dmsGraphicBlockSize.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 5, 0
		}));
		oid = dmsGraphicIndex.node.oid(15);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 1, 15
		}));
		oid = dmsGraphicNumber.node.oid(15);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 2, 15
		}));
		oid = dmsGraphicName.node.oid(15);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 3, 15
		}));
		oid = dmsGraphicHeight.node.oid(15);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 4, 15
		}));
		oid = dmsGraphicWidth.node.oid(15);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 5, 15
		}));
		oid = dmsGraphicType.node.oid(15);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 6, 15
		}));
		oid = dmsGraphicID.node.oid(15);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 7, 15
		}));
		oid = dmsGraphicTransparentEnabled.node.oid(15);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 8, 15
		}));
		oid = dmsGraphicTransparentColor.node.oid(15);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 9, 15
		}));
		oid = dmsGraphicStatus.node.oid(15);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 6, 1, 10, 15
		}));
		oid = dmsGraphicBitmapIndex.node.oid(6, 15);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 7, 1, 1, 6, 15
		}));
		oid = dmsGraphicBlockNumber.node.oid(6, 15);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 7, 1, 2, 6, 15
		}));
		oid = dmsGraphicBlockBitmap.node.oid(6, 15);
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 10, 7, 1, 3, 6, 15
		}));
	}
}
