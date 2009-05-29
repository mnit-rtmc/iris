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

import java.util.Arrays;
import junit.framework.TestCase;

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
		oid = new VmsCharacterHeightPixels().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 1, 0
		}));
		oid = new VmsCharacterWidthPixels().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 2, 0
		}));
		oid = new VmsSignHeightPixels().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 3, 0
		}));
		oid = new VmsSignWidthPixels().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 4, 0
		}));
		oid = new VmsHorizontalPitch().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 5, 0
		}));
		oid = new VmsVerticalPitch().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 2, 6, 0
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
		oid = new DmsShortPowerRecoveryMessage(0, 0, 0).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 8, 0
		}));
		oid = new DmsLongPowerRecoveryMessage(0, 0, 0).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 9, 0
		}));
		oid = new DmsShortPowerLossTime().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 10, 0
		}));
		oid = new DmsCommunicationsLossMessage(0, 0, 0).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 12, 0
		}));
		oid = new DmsTimeCommLoss().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 13, 0
		}));
		oid = new DmsPowerLossMessage(0, 0, 0).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 14, 0
		}));
		oid = new DmsEndDurationMessage(0, 0, 0).getOID();
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
		oid = new VmsPixelServiceDuration().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 21, 0
		}));
		oid = new VmsPixelServiceFrequency().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 22, 0
		}));
		oid = new VmsPixelServiceTime().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 6, 23, 0
		}));

		oid = new TempMinCtrlCabinet().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 1, 0
		}));
		oid = new TempMaxCtrlCabinet().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 2, 0
		}));
		oid = new TempMinAmbient().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 3, 0
		}));
		oid = new TempMaxAmbient().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 4, 0
		}));
		oid = new TempMinSignHousing().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 5, 0
		}));
		oid = new TempMaxSignHousing().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 3, 9, 9, 6, 0
		}));

		oid = new IllumPowerStatus().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 9, 2, 0
		}));
		oid = new SignFaceHeatStatus().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 9, 4, 0
		}));
		oid = new SensorFailures().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 9, 17, 0
		}));

		oid = new DynBrightDayNight().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 1, 1, 0
		}));
		oid = new DynBrightDayRate().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 1, 2, 0
		}));
		oid = new DynBrightNightRate().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 1, 3, 0
		}));
		oid = new DynBrightMaxNightManLvl().getOID();
for(int i: oid) System.err.print(" " + i);
System.err.println();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 1, 8, 0
		}));
		oid = new DmsTempCritical().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 1, 11, 0
		}));
	}
}
