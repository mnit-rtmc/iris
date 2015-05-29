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
package us.mn.state.dot.tms.server.comm.ntcip.mibskyline;

import java.util.Arrays;
import junit.framework.TestCase;
import static us.mn.state.dot.tms.server.comm.ntcip.mibskyline.MIB.*;

/** 
 * OID tests.
 *
 * @author Doug Lau
 */
public class OIDTest extends TestCase {

	public OIDTest(String name) {
		super(name);
	}

	public void test() {
		int[] oid;
		oid = dynBrightDayNight.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 1, 1, 0
		}));
		oid = dynBrightDayRate.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 1, 2, 0
		}));
		oid = dynBrightNightRate.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 1, 3, 0
		}));
		oid = dynBrightMaxNightManLvl.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 1, 8, 0
		}));
		oid = dmsTempCritical.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 1, 11, 0
		}));

		oid = illumPowerStatus.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 9, 2, 0
		}));
		oid = signFaceHeatStatus.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 9, 4, 0
		}));
		oid = sensorFailures.node.oid();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 18, 2, 3, 9, 17, 0
		}));
	}
}
