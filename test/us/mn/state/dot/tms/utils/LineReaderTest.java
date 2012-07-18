/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
import java.io.StringReader;
import junit.framework.TestCase;

/** 
 * @author Douglas Lau
 */
public class LineReaderTest extends TestCase {

	public LineReaderTest(String name) {
		super(name);
	}

	public void test() {
		StringReader sr = new StringReader("1\n2\n3\r4\r\n5\n\n7");
		LineReader lr = new LineReader(sr, 32);
		try {
			assertTrue(lr.readLine().equals("1"));
			assertTrue(lr.readLine().equals("2"));
			assertTrue(lr.readLine().equals("3"));
			assertTrue(lr.readLine().equals("4"));
			assertTrue(lr.readLine().equals("5"));
			assertTrue(lr.readLine().equals(""));
			assertTrue(lr.readLine().equals("7"));
			assertTrue(lr.readLine() == null);
		}
		catch(IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void test2() {
		StringReader sr = new StringReader(
			"123456\n123456\r123456\r\n123456\n1234567890");
		LineReader lr = new LineReader(sr, 8);
		try {
			for(int i = 0; i < 4; i++)
				assertTrue(lr.readLine().equals("123456"));
		}
		catch(IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		try {
			lr.readLine();
			assertTrue(false);
		}
		catch(IOException e) {
			assertTrue(e.getMessage().equals(
				"LineReader buffer full"));
		}
	}

	public void test3() {
		// Test windows-separator on buffer read boundary
		StringReader sr = new StringReader(
			"1234567\r\n123456");
		LineReader lr = new LineReader(sr, 8);
		try {
			assertTrue(lr.readLine().equals("1234567"));
			assertTrue(lr.readLine().equals("123456"));
			assertTrue(lr.readLine() == null);
		}
		catch(IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
