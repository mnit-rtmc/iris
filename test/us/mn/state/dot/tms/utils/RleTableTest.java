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
 * RleTable test cases
 * @author Doug Lau
 */
public class RleTableTest extends TestCase {

	public RleTableTest(String name) {
		super(name);
	}

	public void testEmpty() {
		try {
			int v = new RleTable("").decode();
			assertTrue(false);
		}
		catch (IndexOutOfBoundsException e) { }
	}

	public void testEncode() {
		RleTable enc = new RleTable();
		enc.encode(0);
		enc.encode(0);
		enc.encode(1);
		enc.encode(1);
		enc.encode(1);
		enc.encode(1);
		assertTrue(enc.toString().equals("0113"));
	}

	public void testEncode2() {
		RleTable enc = new RleTable();
		for (int i = 0; i < 37; i++)
			enc.encode(37);
		assertTrue(enc.toString().equals("Z1Y1"));
	}

	public void testDecode() {
		RleTable dec = new RleTable("1024");
		assertTrue(dec.decode() == 1);
		assertTrue(dec.decode() == 2);
		assertTrue(dec.decode() == 2);
		assertTrue(dec.decode() == 2);
		assertTrue(dec.decode() == 2);
		assertTrue(dec.decode() == 2);
	}

	public void testDecode2() {
		RleTable dec = new RleTable("Z1Y1");
		for (int i = 0; i < 37; i++)
			assertTrue(dec.decode() == 37);
	}

	public void testCodec() {
		RleTable enc = new RleTable();
		for (int i = 0; i < 5000; i++)
			enc.encode(i);
		assertTrue(enc.toString().length() == 18944);
		for (int i = 0; i < 5000; i++)
			assertTrue(enc.decode() == i);
	}
}
