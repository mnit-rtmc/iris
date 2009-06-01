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
package us.mn.state.dot.tms.server.comm.ntcip.mib1201;

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
		oid = new GlobalMaxModules().getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 6, 1, 2, 0
		}));
		oid = new ModuleMake(7).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 6, 1, 3, 1, 3, 7
		}));
		oid = new ModuleModel(7).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 6, 1, 3, 1, 4, 7
		}));
		oid = new ModuleVersion(7).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 6, 1, 3, 1, 5, 7
		}));
		oid = new ModuleType(7).getOID();
		assertTrue(Arrays.equals(oid, new int[] {
			1, 3, 6, 1, 4, 1, 1206, 4, 2, 6, 1, 3, 1, 6, 7
		}));
	}
}
