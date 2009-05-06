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

package us.mn.state.dot.tms.utils;

import junit.framework.TestCase;

/** 
 * HexString test cases
 * @author Michael Darter, AHMCT
 * @created 05/06/09
 */
public class HexStringTest extends TestCase {

	/** constructor */
	public HexStringTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		// charToByte
		assertTrue(HexString.charToByte('0') == 0);
		assertTrue(HexString.charToByte('1') == 1);
		assertTrue(HexString.charToByte('2') == 2);
		assertTrue(HexString.charToByte('3') == 3);
		assertTrue(HexString.charToByte('4') == 4);
		assertTrue(HexString.charToByte('5') == 5);
		assertTrue(HexString.charToByte('a') == 10);
		assertTrue(HexString.charToByte('B') == 11);
		assertTrue(HexString.charToByte('c') == 12);
		assertTrue(HexString.charToByte('D') == 13);
		assertTrue(HexString.charToByte('e') == 14);
		assertTrue(HexString.charToByte('F') == 15);

		// hexToByte
		assertTrue(HexString.hexToByte('1', '1') == 17);
		assertTrue(HexString.hexToByte('f', 'f') == 255);
		assertTrue(HexString.hexToByte('0', '0') == 0);
		assertTrue(HexString.hexToByte('1', '0') == 16);
		assertTrue(HexString.hexToByte('0', '1') == 1);
		assertTrue(HexString.hexToByte('E', 'E') == 238);

		// isEven
		assertTrue(HexString.isEven(0));
		assertTrue(HexString.isEven(2));
		assertTrue(HexString.isEven(-2));
		assertTrue(!HexString.isEven(1));
		assertTrue(!HexString.isEven(3));

		// toHexString
		assertTrue(HexString.toHexString((byte) 0).
			compareToIgnoreCase("00") == 0);
		assertTrue(HexString.toHexString((byte) 1).
			compareToIgnoreCase("01") == 0);
		assertTrue(HexString.toHexString((byte) 10).
			compareToIgnoreCase("0A") == 0);
		assertTrue(HexString.toHexString((byte) 11).
			compareToIgnoreCase("0B") == 0);
		assertTrue(HexString.toHexString((byte) 12).
			compareToIgnoreCase("0C") == 0);
		assertTrue(HexString.toHexString((byte) 13).
			compareToIgnoreCase("0D") == 0);
		assertTrue(HexString.toHexString((byte) 14).
			compareToIgnoreCase("0E") == 0);
		assertTrue(HexString.toHexString((byte) 15).
			compareToIgnoreCase("0F") == 0);
		assertTrue(HexString.toHexString((byte) 16).
			compareToIgnoreCase("10") == 0);
		assertTrue(HexString.toHexString((byte) 254).
			compareToIgnoreCase("FE") == 0);
		assertTrue(HexString.toHexString((byte) 255).
			compareToIgnoreCase("FF") == 0);

		// appendToHexString
		StringBuilder sb = new StringBuilder(0);
		sb = HexString.appendToHexString(sb, (byte) 255);
		assertTrue(sb.length() == 2);
		assertTrue(sb.toString().
			compareToIgnoreCase("FF") == 0);
		sb = HexString.appendToHexString(sb, (byte) 254);
		assertTrue(sb.length() == 4);
		assertTrue(sb.toString().
			compareToIgnoreCase("FFFE") == 0);

		// hexStringToByteArray
		byte[] a = HexString.hexStringToByteArray("0001090a0A0b0fFFfe");
		assertTrue(a.length == 9);
		assertTrue(a[0] == 0);
		assertTrue(a[1] == 1);
		assertTrue(a[2] == 9);
		assertTrue(a[3] == 10);
		assertTrue(a[4] == 10);
		assertTrue(a[5] == 11);
		assertTrue(a[6] == 15);
		assertTrue(a[7] == (byte) 255);
		assertTrue(a[8] == (byte) 254);
	}
}
