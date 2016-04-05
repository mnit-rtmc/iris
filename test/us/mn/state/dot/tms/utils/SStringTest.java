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
		assertTrue(!SString.containsChar(null,'x'));
		assertTrue(!SString.containsChar("",'x'));
		assertTrue(SString.containsChar("abcdx",'x'));
		assertTrue(!SString.containsChar("abcd",'x'));
	}

	public void testUnion() {
		assertTrue(SString.union(null,"x")==null);
		assertTrue(SString.union("abc",null)==null);
		assertTrue(SString.union("abc","").equals(""));
		assertTrue(SString.union("abcdefg","aceg").equals("aceg"));
		assertTrue(SString.union("abcdefg",
			"0123aceg123").equals("aceg"));
	}

	public void testTruncate() {
		assertTrue(SString.truncate(null,0).equals(""));
		assertTrue(SString.truncate(null,5).equals(""));
		assertTrue(SString.truncate("",0).equals(""));
		assertTrue(SString.truncate("",3).equals(""));
		assertTrue(SString.truncate("abcdef",0).equals(""));
		assertTrue(SString.truncate("abcdef",1).equals("a"));
		assertTrue(SString.truncate("abcdef",2).equals("ab"));
		assertTrue(SString.truncate("abcdef",3).equals("abc"));
		assertTrue(SString.truncate("abcdef",35).equals("abcdef"));
	}

	public void testToRightField() {
		assertTrue((new String("").compareTo(
			SString.toRightField("", "")) == 0));
		assertTrue((new String("1234a").compareTo(
			SString.toRightField("12345", "a")) == 0));
		assertTrue((new String("1abcd").compareTo(
			SString.toRightField("12345", "abcd")) == 0));
		assertTrue((new String("12345").compareTo(
			SString.toRightField("12345", "")) == 0));
		assertTrue((new String("abcdef").compareTo(
			SString.toRightField("123456", "abcdef")) == 0));
	}

	public void testRemoveEnclosingQuotes() {
		assertTrue((new String("abcd").compareTo(
			SString.removeEnclosingQuotes("abcd")) == 0));
		assertTrue((new String("abcd").compareTo(
			SString.removeEnclosingQuotes("\"abcd\"")) == 0));
		assertTrue((new String("").compareTo(
			SString.removeEnclosingQuotes("")) == 0));
		assertTrue((null == SString.removeEnclosingQuotes(null)));
		assertTrue((new String("\"abcd\" ").compareTo(
			SString.removeEnclosingQuotes("\"abcd\" ")) == 0));
		assertTrue((new String("x").compareTo(
			SString.removeEnclosingQuotes("\"x\"")) == 0));
	}

	public void testAlphaPrefixLen() {
		assertTrue(SString.alphaPrefixLen(null, "abcd") == 0);
		assertTrue(SString.alphaPrefixLen("1234", null) == 0);
		assertTrue(SString.alphaPrefixLen("", "abcd") == 0);
		assertTrue(SString.alphaPrefixLen("abcd", "1234") == 0);
		assertTrue(SString.alphaPrefixLen("abcd", "a1234") == 1);
		assertTrue(SString.alphaPrefixLen("abc", "abcdef") == 3);
		assertTrue(SString.alphaPrefixLen("abcdef", "abc") == 3);
		assertTrue(SString.alphaPrefixLen("abcdef", "abcdef") == 6);
		assertTrue(SString.alphaPrefixLen("abcdef1234", "abcdef1234") == 6);
		assertTrue(SString.alphaPrefixLen("!@#$%3", "!@#$%3") == 5);
		assertTrue(SString.alphaPrefixLen("1234", "1234") == 0);
	}

	public void testCount() {
		assertTrue(0 == SString.count(null, null));
		assertTrue(0 == SString.count("", ""));
		assertTrue(0 == SString.count("abc", ""));
		assertTrue(1 == SString.count("abc", "c"));
		assertTrue(1 == SString.count("abc", "bc"));
		assertTrue(4 == SString.count("aaaa", "a"));
		assertTrue(0 == SString.count("abc", "bcd"));
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
}
