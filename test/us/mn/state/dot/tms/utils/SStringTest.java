/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009 - 2011  AHMCT, University of California
 * Copyright (C) 2016  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.utils.SString.*;

/** 
 * SString test cases
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class SStringTest extends TestCase {

	public SStringTest(String name) {
		super(name);
	}

	public void testContainsChar() {
		assertTrue(!containsChar(null, 'x'));
		assertTrue(!containsChar("", 'x'));
		assertTrue(containsChar("abcdx", 'x'));
		assertTrue(!containsChar("abcd", 'x'));
	}

	public void testUnion() {
		assertTrue(union(null, "x") == null);
		assertTrue(union("abc", null) == null);
		assertTrue(union("abc", "").equals(""));
		assertTrue(union("abcdefg", "aceg").equals("aceg"));
		assertTrue(union("abcdefg", "0123aceg123").equals("aceg"));
	}

	public void testTruncate() {
		assertTrue(truncate(null, 0).equals(""));
		assertTrue(truncate(null, 5).equals(""));
		assertTrue(truncate("", 0).equals(""));
		assertTrue(truncate("", 3).equals(""));
		assertTrue(truncate("abcdef", 0).equals(""));
		assertTrue(truncate("abcdef", 1).equals("a"));
		assertTrue(truncate("abcdef", 2).equals("ab"));
		assertTrue(truncate("abcdef", 3).equals("abc"));
		assertTrue(truncate("abcdef", 35).equals("abcdef"));
	}

	public void testToRightField() {
		assertTrue((new String("").compareTo(
			toRightField("", "")) == 0));
		assertTrue((new String("1234a").compareTo(
			toRightField("12345", "a")) == 0));
		assertTrue((new String("1abcd").compareTo(
			toRightField("12345", "abcd")) == 0));
		assertTrue((new String("12345").compareTo(
			toRightField("12345", "")) == 0));
		assertTrue((new String("abcdef").compareTo(
			toRightField("123456", "abcdef")) == 0));
	}

	public void testRemoveEnclosingQuotes() {
		assertTrue((new String("abcd").compareTo(
			removeEnclosingQuotes("abcd")) == 0));
		assertTrue((new String("abcd").compareTo(
			removeEnclosingQuotes("\"abcd\"")) == 0));
		assertTrue((new String("").compareTo(
			removeEnclosingQuotes("")) == 0));
		assertTrue((null == removeEnclosingQuotes(null)));
		assertTrue((new String("\"abcd\" ").compareTo(
			removeEnclosingQuotes("\"abcd\" ")) == 0));
		assertTrue((new String("x").compareTo(
			removeEnclosingQuotes("\"x\"")) == 0));
	}

	public void testAlphaPrefixLen() {
		assertTrue(alphaPrefixLen(null, "abcd") == 0);
		assertTrue(alphaPrefixLen("1234", null) == 0);
		assertTrue(alphaPrefixLen("", "abcd") == 0);
		assertTrue(alphaPrefixLen("abcd", "1234") == 0);
		assertTrue(alphaPrefixLen("abcd", "a1234") == 1);
		assertTrue(alphaPrefixLen("abc", "abcdef") == 3);
		assertTrue(alphaPrefixLen("abcdef", "abc") == 3);
		assertTrue(alphaPrefixLen("abcdef", "abcdef") == 6);
		assertTrue(alphaPrefixLen("abcdef1234", "abcdef1234") == 6);
		assertTrue(alphaPrefixLen("!@#$%3", "!@#$%3") == 5);
		assertTrue(alphaPrefixLen("1234", "1234") == 0);
	}

	public void testCount() {
		assertTrue(0 == count(null, null));
		assertTrue(0 == count("", ""));
		assertTrue(0 == count("abc", ""));
		assertTrue(1 == count("abc", "c"));
		assertTrue(1 == count("abc", "bc"));
		assertTrue(4 == count("aaaa", "a"));
		assertTrue(0 == count("abc", "bcd"));
	}

	public void testLongestCommonSubstring() {
		assertTrue(longestCommonSubstring("", "").equals(""));
		assertTrue(longestCommonSubstring("a", "").equals(""));
		assertTrue(longestCommonSubstring("a", "a").equals("a"));
		assertTrue(longestCommonSubstring("ab", "a").equals("a"));
		assertTrue(longestCommonSubstring("ab", "ab").equals("ab"));
		assertTrue(longestCommonSubstring("abc", "ab").equals("ab"));
		assertTrue(longestCommonSubstring("abcd", "ab").equals("ab"));
		assertTrue(longestCommonSubstring("abcd",
			"abcd").equals("abcd"));
		assertTrue(longestCommonSubstring("__123__abcd___",
			"xabcdx").equals("abcd"));
		assertTrue(longestCommonSubstring("22abcd33",
			"__123__abcd__").equals("abcd"));
	}

	public void testContainsDigit() {
		assertFalse(containsDigit(""));
		assertFalse(containsDigit(" \t[]\\{}|"));
		assertFalse(containsDigit("!@#$%^&*()_+`~-+,./<>?;':"));
		assertTrue(containsDigit("1"));
		assertTrue(containsDigit("234"));
		assertTrue(containsDigit("567"));
		assertTrue(containsDigit("890"));
		assertFalse(containsDigit("abc"));
		assertFalse(containsDigit("XYZ"));
		assertFalse(containsDigit("abcdefghijklmnopqrstuvwxyz"));
		assertFalse(containsDigit("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
	}

	public void testContainsLetter() {
		assertFalse(containsLetter(""));
		assertFalse(containsLetter(" \t[]\\{}|"));
		assertFalse(containsLetter("!@#$%^&*()_+`~-+,./<>?;':"));
		assertFalse(containsLetter("1"));
		assertFalse(containsLetter("234"));
		assertFalse(containsLetter("567"));
		assertFalse(containsLetter("890"));
		assertTrue(containsLetter("abc"));
		assertTrue(containsLetter("XYZ"));
		assertTrue(containsLetter("abcdefghijklmnopqrstuvwxyz"));
		assertTrue(containsLetter("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
	}
}
