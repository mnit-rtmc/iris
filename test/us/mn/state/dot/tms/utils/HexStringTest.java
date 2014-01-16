/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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

import java.util.Arrays;
import junit.framework.TestCase;

/** 
 * HexString test cases
 *
 * @author Michael Darter, AHMCT
 * @author Douglas Lau
 */
public class HexStringTest extends TestCase {

	public HexStringTest(String name) {
		super(name);
	}

	static private byte[] toBytes(int[] idata) {
		byte[] data = new byte[idata.length];
		for(int i = 0; i < data.length; i++)
			data[i] = (byte)idata[i];
		return data;
	}

	static private void checkFormat1(int idata, String hex) {
		byte data = (byte)idata;
		assertTrue(HexString.format(data).equals(hex));
	}

	public void testFormat1() {
		checkFormat1(0, "00");
		checkFormat1(1, "01");
		checkFormat1(10, "0A");
		checkFormat1(11, "0B");
		checkFormat1(12, "0C");
		checkFormat1(13, "0D");
		checkFormat1(14, "0E");
		checkFormat1(15, "0F");
		checkFormat1(16, "10");
		checkFormat1(254, "FE");
		checkFormat1(255, "FF");
	}

	static private void checkFormat(int[] idata, char delim, String hex) {
		byte[] data = toBytes(idata);
		assertTrue(HexString.format(data, delim).equals(hex));
	}

	static private void checkFormat(int[] idata, String hex) {
		byte[] data = toBytes(idata);
		assertTrue(HexString.format(data).equals(hex));
	}

	public void testFormat() {
		checkFormat(new int[] { 0 }, "00");
		checkFormat(new int[] { 255 }, "FF");
		checkFormat(new int[] { 0, 127, 128, 255 }, "007F80FF");
		checkFormat(new int[] { 0, 127, 128, 255, 0 }, "007F80FF00");
	}

	public void testFormatDelim() {
		checkFormat(new int[] { 0 }, ' ', "00");
		checkFormat(new int[] { 255 }, ' ', "FF");
		checkFormat(new int[] { 0, 127, 128, 255 }, ' ',"00 7F 80 FF");
		checkFormat(new int[] { 0, 127, 128, 255 }, ':',"00:7F:80:FF");
	}

	private void checkParse(String hex, int[] idata) {
		byte[] data = toBytes(idata);
		assertTrue(Arrays.equals(HexString.parse(hex), data));
	}

	private void checkParseFail(String hex, int[] idata) {
		try {
			checkParse(hex, idata);
			assertFalse(true);
		}
		catch(Exception e) {
			// expected
		}
	}

	public void testParse() {
		checkParse("", new int[0]);
		checkParse("00", new int[] { 0, } );
		checkParse("01", new int[] { 1, } );
		checkParse("0A", new int[] { 10, } );
		checkParse("FF", new int[] { 255, } );
		checkParse("7F", new int[] { 127, } );
		checkParse("0001090a0A0b0fFFfe", new int[] { 0, 1, 9, 10, 10,
			11, 15, 255, 254 } );
		checkParseFail(null, new int[] { 0, });
		checkParseFail("1", new int[] { 0, });
		checkParseFail("ABC", new int[] { 0, });
		checkParseFail("GG", new int[] { 0, });
		checkParseFail("==", new int[] { 0, });
	}
}
