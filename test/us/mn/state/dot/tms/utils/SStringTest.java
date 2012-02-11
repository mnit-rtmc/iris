/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009 - 2011  AHMCT, University of California
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
 * SString test cases
 * @author Michael Darter
 * @created 05/05/09
 * @see SString
 */
public class SStringTest extends TestCase {

	/** constructor */
	public SStringTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		// containsChar
		assertTrue(!SString.containsChar(null,'x'));
		assertTrue(!SString.containsChar("",'x'));
		assertTrue(SString.containsChar("abcdx",'x'));
		assertTrue(!SString.containsChar("abcd",'x'));

		// union
		assertTrue(SString.union(null,"x")==null);
		assertTrue(SString.union("abc",null)==null);
		assertTrue(SString.union("abc","").equals(""));
		assertTrue(SString.union("abcdefg","aceg").equals("aceg"));
		assertTrue(SString.union("abcdefg",
			"0123aceg123").equals("aceg"));

		// truncate
		assertTrue(SString.truncate(null,0).equals(""));
		assertTrue(SString.truncate(null,5).equals(""));
		assertTrue(SString.truncate("",0).equals(""));
		assertTrue(SString.truncate("",3).equals(""));
		assertTrue(SString.truncate("abcdef",0).equals(""));
		assertTrue(SString.truncate("abcdef",1).equals("a"));
		assertTrue(SString.truncate("abcdef",2).equals("ab"));
		assertTrue(SString.truncate("abcdef",3).equals("abc"));
		assertTrue(SString.truncate("abcdef",35).equals("abcdef"));

		// toRightField
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

		// removeEnclosingQuotes
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

		// alphaPrefixLen
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

		// count
		assertTrue(0 == SString.count(null, null));
		assertTrue(0 == SString.count("", ""));
		assertTrue(0 == SString.count("abc", ""));
		assertTrue(1 == SString.count("abc", "c"));
		assertTrue(1 == SString.count("abc", "bc"));
		assertTrue(4 == SString.count("aaaa", "a"));
		assertTrue(0 == SString.count("abc", "bcd"));
	}
}
