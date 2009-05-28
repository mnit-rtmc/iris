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
 * Test cases
 * @author Michael Darter
 * @created 05/07/09
 */
public class NumericAlphaComparatorTest extends TestCase {

	/** constructor */
	public NumericAlphaComparatorTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		NumericAlphaComparator c = new NumericAlphaComparator();

		// bogus args
		assertTrue(c.compare(null, null) == 0);
		assertTrue(c.compare("", "") == 0);
		assertTrue(c.compare("", null) < 0);
		assertTrue(c.compare("null", null) == 0);

		// both numeric
		assertTrue(c.compare("123", "123") == 0);
		assertTrue(c.compare("12", "123") < 0);
		assertTrue(c.compare("23", "123") < 0);
		assertTrue(c.compare("123", "23") > 0);
		assertTrue(c.compare("0", "00") < 0);
		assertTrue(c.compare("0123", "123") < 0);

		// both alpha
		assertTrue(c.compare("abc", "abc") == 0);
		assertTrue(c.compare("abb", "abc") < 0);
		assertTrue(c.compare("aa", "aaa") < 0);
		assertTrue(c.compare("aa", "a") > 0);

		// alpha numeric
		assertTrue(c.compare("1abc", "1abc") == 0);
		assertTrue(c.compare("abc1", "abc1") == 0);
		assertTrue(c.compare("123", "123 ") < 0);
		assertTrue(c.compare("ab1", "abc") < 0);
		assertTrue(c.compare("abc", "ab1") > 0);
		assertTrue(c.compare("abc01", "abc1") < 0);

		// shared alpha prefix
		assertTrue(c.compare("a2", "a10") < 0);
		assertTrue(c.compare("abc200", "abc200") == 0);
		assertTrue(c.compare("abc200", "abc202") < 0);
		assertTrue(c.compare("abc0200", "abc200") < 0);
		assertTrue(c.compare("abc200", "abc200a") < 0);
	}
}
