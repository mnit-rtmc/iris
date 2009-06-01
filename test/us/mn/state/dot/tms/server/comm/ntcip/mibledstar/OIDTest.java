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
package us.mn.state.dot.tms.server.comm.ntcip.mibledstar;

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
		oid = new LedHighTempCutoff().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 16, 1, 1, 1, 0
		}));
		oid = new LedSignErrorOverride().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 16, 1, 1, 2, 0
		}));
		oid = new LedBadPixelLimit().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 16, 1, 1, 3, 0
		}));
		oid = new LedLdcPotBase().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 16, 1, 1, 6, 0
		}));
		oid = new LedPixelLow().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 16, 1, 1, 7, 0
		}));
		oid = new LedPixelHigh().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 16, 1, 1, 8, 0
		}));
		oid = new LedActivateMsgError().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 3, 16, 1, 2, 12, 0
		}));
	}
}
