/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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
 * Pint64 test cases
 * @author Doug Lau
 */
public class Pint64Test extends TestCase {

	public Pint64Test(String name) {
		super(name);
	}

	public void testEmpty() {
		try {
			int v = new Pint64("").decode();
			assertTrue(false);
		}
		catch (IndexOutOfBoundsException e) { }
	}

	public void testEncode() {
		Pint64 enc = new Pint64();
		enc.encode(0);
		enc.encode(1);
		enc.encode(2);
		enc.encode(3);
		assertTrue(enc.toString().equals("0123"));
	}

	public void testEncode2() {
		Pint64 enc = new Pint64();
		enc.encode(31);
		enc.encode(32);
		assertTrue(enc.toString().equals("TU1"));
	}

	public void testDecode() {
		Pint64 dec = new Pint64("4567");
		assertTrue(dec.decode() == 4);
		assertTrue(dec.decode() == 5);
		assertTrue(dec.decode() == 6);
		assertTrue(dec.decode() == 7);
	}

	public void testDecode2() {
		Pint64 dec = new Pint64("TU1");
		assertTrue(dec.decode() == 31);
		assertTrue(dec.decode() == 32);
	}

	public void testCodec() {
		Pint64 enc = new Pint64();
		for (int i = 0; i < 5000; i++)
			enc.encode(i);
		assertTrue(enc.toString().length() == 13944);
		for (int i = 0; i < 5000; i++)
			assertTrue(enc.decode() == i);
	}
}
