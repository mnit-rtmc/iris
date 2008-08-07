/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

/**
 * String convenience methods.
 *
 * @author Michael Darter
 */
public class SString {

	/** instance can't be created */
	private SString(){}

	/**
	 *  test methods.
	 */
	static public boolean test() {
		boolean ok = true;

		// containsChar
		ok = ok & !containsChar(null,'x');
		ok = ok & !containsChar("",'x');
		ok = ok & containsChar("abcdx",'x');
		ok = ok & !containsChar("abcd",'x');

		// union
		ok = ok & union(null,"x")==null;
		ok = ok & union("abc",null)==null;
		ok = ok & union("abc","").equals("");
		ok = ok & union("abcdefg","aceg").equals("aceg");
		ok = ok & union("abcdefg","0123aceg123").equals("aceg");

		return (ok);
	}

	/**
	 *  Does a string contain the specified char?
	 *  @param str string to search
	 *  @param c character to search for
	 *  @return true if str contains c else false.
	 */
	public static boolean containsChar(String str,char c) {
		if (str==null)
			return false;
		for (int i=0; i<str.length(); ++i) {
			if (str.charAt(i)==c)
				return true;
		}
		return false;
	}

	/**
	 *  Return a string that contains the union of characters in two
	 *  strings. This method can be used to validate a string. e.g. 
	 *  "abcd" and "1b3d" will return "bd".
	 *  @param str string to validate
	 *  @param valid string that contains valid chars.
	 *  @return Argument str containing only characters found in arg valid.
	 */
	public static String union(String str,String valid) {
		if (str==null || valid==null)
			return null;
		if (str.length()<=0 || valid.length()<=0)
			return "";
		StringBuilder ret=new StringBuilder(str.length());
		for (int i=0; i<str.length(); ++i) {
			if (SString.containsChar(valid,str.charAt(i)))
				ret.append(str.charAt(i));
		}
		return ret.toString();
	}

	/**
	 * convert byte[] to char[] using specific encoding.
	 * @returns An empty string on error.
	 */
	public static String byteArrayToString(byte[] b) {
		int len=(b==null ? 0 : b.length);
		return byteArrayToString(b,len);
	}

	/**
	 * convert byte[] to char[] using specific encoding.
	 * @returns An empty string on error.
	 */
	public static String byteArrayToString(byte[] b, int len) {

		// validate args
		if (b==null || b.length<=0 || len<=0)
			return "";
		if (b.length<len)
			len=b.length;

		String s = "";
		try {
			s = new String(b, 0, len, "ISO-8859-1");
		} catch (Exception UnsupportedEncodingException) {
			s = "";
		}
		return s;
	}

	/**
	 *  Return a string with the enclosing double quotes removed.
	 *  This method assumes the first and last chars are \" and
	 *  if not the string is returned unmodified.
	 */
	static public String removeEnclosingQuotes(String s) {
		if(s == null)
			return (null);
		if((s.length() >= 2) && (s.charAt(0) == '\"')
			&& (s.charAt(s.length() - 1) == '\"'))
			return (s.substring(1, s.length() - 1));
		return (s);
	}

	/** return true if the specified string is enclosed by another string */
	public static boolean enclosedBy(String s,String e) {
		if (s==null || e==null)
			return false;
		return s.startsWith(e) && s.endsWith(e);
	}

	/**
	 *  Convert an int to string with the specified number
	 *  of digits, prefixing with zeros as necessary.
	 *  e.g. (4,2) returns '04', (666,2) returns 666.
	 */
	public static String intToString(int i, int numdigs) {
		String s = String.valueOf(i);
		int numzerostoadd = numdigs - s.length();
		if(numzerostoadd > 0)
			for(int j = 0; j < numzerostoadd; ++j)
				s = "0" + s;
		return (s);
	}
}

