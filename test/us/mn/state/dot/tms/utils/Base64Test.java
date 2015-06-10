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
package us.mn.state.dot.tms.utils;

import java.io.IOException;
import junit.framework.TestCase;

/** 
 * Base64 test cases
 * @author Doug Lau
 * @author Michael Darter
 */
public class Base64Test extends TestCase {

	public Base64Test(String name) {
		super(name);
	}

	public void testEmpty() {
		try {
			byte[] m = Base64.decode("");
			assertTrue(m.length == 0);
		}
		catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void test() {
		// encode, decode
		for (int l = 1; l < 80; l++) {
			byte[] m = new byte[l];
			for (int i = 0; i < 256; i++) {
				for (int j = 0; j < l; j++)
					m[j] = (byte)i;
				// round-trip encode, decode, compare result
				String v = Base64.encode(m);
				try {
					byte[] b = Base64.decode(v);
					assertTrue("l=" + l + ", i=" + i, 
						java.util.Arrays.equals(b, m));
				}
				catch (IOException e) {
					e.printStackTrace();
					assertTrue(false);
				}
			}
		}
	}
}
