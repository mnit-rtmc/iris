/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2011  AHMCT, University of California
 * Copyright (C) 2013  Minnesota Department of Transportation
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
 * @see SStringTest
 */
public class SString {

	/** instance can't be created */
	private SString(){}

	/** Count the number of specified characters in the string.
	 * @param s String to count chars in, may be null. */
	public static int count(String s, char c) {
		if(s == null)
			return 0;
		int cn = 0;
		for(int i = 0; i < s.length(); ++i)
			if(s.charAt(i) == c)
				++cn;
		return cn;
	}

	/** Count the number of times the specified string occurs 
	 * in another string.
	 * @param s String to count occurences in, may be null.
	 * @param c String to count occurences of, may be null.
	 * @return Number of times c occurs in s. */
	public static int count(String s, String c) {
		if(s == null || c == null || s.isEmpty() || c.isEmpty())
			return 0;
		int n = 0;
		for(int i = 0; i < s.length(); ++i)
			if(s.startsWith(c, i))
				++n;
		return n;
	}

	/** Does a string contain the specified char?
	 *  @param s String to search, may be null.
	 *  @param c Character to search for.
	 *  @return True if s contains c else false. */
	public static boolean containsChar(String s,char c) {
		return count(s, c) > 0;
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

	/** Truncate a string to a given maximum length.
	 * @param arg String to be truncated.
	 * @param maxlen Maximum length of string (characters).
	 * @return Truncated string. */
	static public String truncate(String arg, int maxlen) {
		arg = (arg == null) ? "" : arg;
		return arg.substring(0, Math.min(arg.length(),
			Math.max(0, maxlen)));
	}

	/**
	 *  Given a filled field and string, return a string 
	 *  containing the field with the string right justified.
	 *  e.g. ("0000","XY") returns "00XY".
	 */
	static public String toRightField(String f, String s) {
		if (!((f != null) && (s != null)))
			throw new IllegalArgumentException(
				"SString.toRightField: arg f or s is null.");
		if (!(f.length() >= s.length()))
	    		throw new IllegalArgumentException("SString." +
				"toRightField: arg length problem:" + 
				f + "," + s);
		int end = f.length() - s.length();
		String ret = f.substring(0, end) + s;
		return (ret);
	}

	/** convert string to int. This method suppresses all number format
	 *  exceptions, returning 0 if a NumberFormatException is caught. */
	public static int stringToInt(String s) {
		if (s == null)
		    return (0);
		int i = 0;
		try {
		    i = Integer.parseInt(s);
		} catch (Exception e) {}
		return i;
	}

	/** convert string to long. This method suppresses all number format
	 *  exceptions, returning 0 if a NumberFormatException is caught. */
	public static long stringToLong(String s) {
		if (s == null)
		    return (0);
		long i = 0;
		try {
		    i = Long.parseLong(s);
		} catch (Exception e) {}
		return i;
	}

	/** convert string to double */
	public static double stringToDouble(String s) {
		if (s == null)
		    return (0);
		double d = 0;
		try {
		    d = Double.parseDouble(s);
		} catch (Exception e) {}
		return d;
	}

	/** convert string to boolean */
	public static boolean stringToBoolean(String s) {
		if (s == null)
		    return false;
		boolean b = false;
		try {
		    b = Boolean.parseBoolean(s);
		} catch (Exception e) {}
		return b;
	}

	/** convert boolean to string */
	public static String booleanToString(boolean b) {
		return new Boolean(b).toString();
	}

	/** convert double to string with rounding */
	public static String doubleToString(double d,int numdecplaces) {
		String ret="";
		// full precision
		if (numdecplaces<0)
			ret=new Double(d).toString();
		// zero decimal places
		else if (numdecplaces==0) {
			ret=(new Double(Math.round(d))).toString();
			if (ret.endsWith(".0"))
				ret=ret.replace(".0","");
		} else {
			double mult=Math.pow(10,numdecplaces);
			ret=new Double(Math.round(d*mult)/mult).toString();
		}
		return ret;
	}

	/** convert int to string */
	public static String intToString(int i) {
		return String.valueOf(i);
	}

	/** convert int to string */
	public static String longToString(long i) {
		return String.valueOf(i);
	}

	/**
	 *  Does a string contain another string?
	 *  @return true if string1 contains string2, case insensitive.
	 */
	public static boolean containsIgnoreCase(String arg1, String arg2) {
		if(arg1 == null || arg2 == null)
			return false;
		if(arg1.length() <= 0 || arg2.length() <= 0)
			return false;
		return arg1.toLowerCase().contains(arg2.toLowerCase());
	}

	/**
	 *  If the specified string ends with the specified tail,
	 *  the string is returned with the tail removed.
	 */
	static public String removeTail(String s, String tail) {
		if(s == null)
			return null;
		if(tail == null || tail.isEmpty())
			return s;
		if((s.endsWith(tail)))
			return s.substring(0, s.length() - tail.length());
		return s;
	}

	/** Convert String[] to a comma separated String. Null values are
	 *  not added to the list, empty strings are. */
	public static String toString(String[] s) {
		if(s == null || s.length == 0)
			return "";
		StringBuilder r = new StringBuilder("");
		for(String x : s)
			if(x != null)
				r.append(x).append(", ");
		return SString.removeTail(r.toString(), ", ");
	}

	/** Return a comma separated list given an int array. */
	public static String toString(int[] i) {
		if(i == null || i.length == 0)
			return "";
		StringBuilder r = new StringBuilder("");
		for(int x : i)
			r.append(x).append(", ");
		return SString.removeTail(r.toString(), ", ");
	}

	/** Join two strings with a space and then trim */
	static public String trimJoin(String a, String b) {
		String j = a + " " + b;
		return j.trim();
	}

	/** return true if the argument is numeric */
	static public boolean isNumeric(String s) {
		if(s == null || s.isEmpty())
			return false;
		boolean found_dec = false;
		boolean found_minus = false;
		for(int i = 0; i < s.length(); ++i) {
			if(s.charAt(i) == '.') {
				if(found_dec)
					return false;
				found_dec = true;
				continue;
			} else if(s.charAt(i) == '-') {
				if(found_minus)
					return false;
				found_minus = true;
				continue;
			} else if(!Character.isDigit(s.charAt(i)))
				return false;
		}
		return true;
	}

	/** return the number of alpha prefix characters shared by 2 strings */
	static public int alphaPrefixLen(String a, String b) {
		if(a == null || b == null)
			return 0;
		int len = Math.min(a.length(), b.length());
		for(int i = 0; i < len; ++i) {
			if(a.charAt(i) == b.charAt(i))
				if(!Character.isDigit(a.charAt(i)))
					continue;
			return i;
		}
		return len;
	}

	/** String comparison: starts with ignoring case */
	public static boolean startsWithIgnoreCase(String s,String sw) {
		if(s == null || sw == null)
			return false;
		return s.toLowerCase().startsWith(sw.toLowerCase());
	}
}
