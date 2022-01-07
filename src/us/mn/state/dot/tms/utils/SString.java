/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2011  AHMCT, University of California
 * Copyright (C) 2013-2020  Minnesota Department of Transportation
 * Copyright (C) 2017-2022  Iteris Inc.
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

/**
 * String convenience methods.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class SString {

	/** Instance can't be created */
	private SString() { }

	/** Convert byte[] to char[] using specific encoding.
	 * @return An empty string on error. */
	static public String byteArrayToString(byte[] b) {
		int len = (b == null) ? 0 : b.length;
		return byteArrayToString(b, len);
	}

	/** Convert byte[] to char[] using specific encoding.
	 * @return An empty string on error. */
	static public String byteArrayToString(byte[] b, int len) {
		if (b == null || b.length <= 0 || len <= 0)
			return "";
		if (b.length < len)
			len = b.length;
		try {
			return new String(b, 0, len, "ISO-8859-1");
		}
		catch (Exception UnsupportedEncodingException) {
			return "";
		}
	}

	/** Return true if the specified string is enclosed by another string */
	static public boolean enclosedBy(String s,String e) {
		if (s == null || e == null)
			return false;
		return s.startsWith(e) && s.endsWith(e);
	}

	/** Convert an int to string with the specified number
	 * of digits, prefixing with zeros as necessary.
	 * e.g. (4,2) returns '04', (666,2) returns 666. */
	static public String intToString(int i, int numdigs) {
		String s = String.valueOf(i);
		int numzerostoadd = numdigs - s.length();
		if (numzerostoadd > 0) {
			for (int j = 0; j < numzerostoadd; j++)
				s = "0" + s;
		}
		return (s);
	}

	/** Truncate a string to a given maximum length.
	 * @param arg String to be truncated.
	 * @param maxlen Maximum length of string (characters).
	 * @return Truncated string. */
	static public String truncate(String arg, int maxlen) {
		arg = (arg != null) ? arg : "";
		maxlen = Math.max(0, maxlen);
		return (arg.length() <= maxlen)
		      ? arg
		      : arg.substring(0, maxlen);
	}

	/** Convert string to int.  This method suppresses all number format
	 * exceptions, returning 0 if a NumberFormatException is caught. */
	static public int stringToInt(String s) {
		if (s == null)
			return 0;
		try {
			return Integer.parseInt(s);
		}
		catch (Exception e) {
			return 0;
		}
	}

	/** Convert string to long.  This method suppresses all number format
	 * exceptions, returning 0 if a NumberFormatException is caught. */
	static public long stringToLong(String s) {
		if (s == null)
			return 0;
		try {
			return Long.parseLong(s);
		}
		catch (Exception e) {
			return 0;
		}
	}

	/** Convert string to double */
	static public double stringToDouble(String s) {
		if (s == null)
			return 0;
		try {
			return Double.parseDouble(s);
		}
		catch (Exception e) {
			return 0;
		}
	}

	/** Convert string to boolean */
	static public boolean stringToBoolean(String s) {
		if (s == null)
			return false;
		try {
			return Boolean.parseBoolean(s);
		}
		catch (Exception e) {
			return false;
		}
	}

	/** Convert boolean to string */
	static public String booleanToString(boolean b) {
		return Boolean.valueOf(b).toString();
	}

	/** Convert double to string with rounding */
	static public String doubleToString(double d, int numdecplaces) {
		if (numdecplaces < 0)
			return Double.valueOf(d).toString();
		else if (numdecplaces == 0) {
			String ret = Double.valueOf(Math.round(d)).toString();
			if (ret.endsWith(".0"))
				return ret.replace(".0","");
			else
				return ret;
		} else {
			double mult = Math.pow(10, numdecplaces);
			return Double.valueOf(Math.round(d * mult) / mult)
				.toString();
		}
	}

	/** Convert int to string */
	static public String intToString(int i) {
		return String.valueOf(i);
	}

	/** Convert long to string */
	static public String longToString(long i) {
		return String.valueOf(i);
	}

	/** Does a string contain another string?
	 * @return true if string1 contains string2, case insensitive. */
	static public boolean containsIgnoreCase(String arg1, String arg2) {
		if (arg1 == null || arg2 == null)
			return false;
		if (arg1.length() <= 0 || arg2.length() <= 0)
			return false;
		return arg1.toLowerCase().contains(arg2.toLowerCase());
	}

	/** Convert String[] to a comma separated String. Null values are
	 * not added to the list, empty strings are. */
	static public String toString(String[] s) {
		if (s == null || s.length == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		for (String x : s) {
			if (x != null) {
				if (sb.length() > 0)
					sb.append(", ");
				sb.append(x);
			}
		}
		return sb.toString();
	}

	/** Return a comma separated list given an int array. */
	static public String toString(int[] i) {
		if (i == null || i.length == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		for (int x : i) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(x);
		}
		return sb.toString();
	}

	/** Return true if the argument is numeric */
	static public boolean isNumeric(String s) {
		if (s == null || s.isEmpty())
			return false;
		boolean found_dec = false;
		boolean found_minus = false;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '.') {
				if (found_dec)
					return false;
				found_dec = true;
				continue;
			} else if (s.charAt(i) == '-') {
				if (found_minus)
					return false;
				found_minus = true;
				continue;
			} else if (!Character.isDigit(s.charAt(i)))
				return false;
		}
		return true;
	}

	/** Return the number of alpha prefix characters shared by 2 strings */
	static public int alphaPrefixLen(String a, String b) {
		if (a == null || b == null)
			return 0;
		int len = Math.min(a.length(), b.length());
		for (int i = 0; i < len; i++) {
			if (a.charAt(i) == b.charAt(i)) {
				if (!Character.isDigit(a.charAt(i)))
					continue;
			}
			return i;
		}
		return len;
	}

	/** Find the longest common substring of two strings */
	static public String longestCommonSubstring(String s1, String s2) {
		int[][] len = new int[s1.length() + 1][s2.length() + 1];
		int start = 0;	// start index of substring in s2
		int end = 0;	// end index of substring in s2

		for (int i = 0; i < s1.length(); i++) {
			for (int j = 0; j < s2.length(); j++) {
				int ln = 0;
				while (ln <= i && ln <= j &&
				       s1.charAt(i - ln) == s2.charAt(j - ln))
				{
					ln++;
				}
				int prev_len = Math.max(len[i][j],
					Math.max(len[i + 1][j], len[i][j + 1]));
				if (ln > prev_len) {
					start = j - ln + 1;
					end = j + 1;
					len[i + 1][j + 1] = ln;
				} else
					len[i + 1][j + 1] = prev_len;
			}
		}
		return s2.substring(start, end);
	}

	/** Count the number of letters in a string */
	static public int countLetters(String s) {
		int n = 0;
		for (int i = 0; i < s.length(); i++) {
			if (Character.isLetter(s.charAt(i)))
				n++;
		}
		return n;
	}

	/** Count the number of unique characters in a string */
	static public int countUnique(String s) {
		char[] u = s.toCharArray();
		char prev = '\0';
		int n = 0;
		Arrays.sort(u);
		for (char c : u) {
			if (c != prev)
				n++;
			prev = c;
		}
		return n;
	}

	/** Check if a string blank or null */
	static public boolean isBlank(String s) {
		return s == null || s.isEmpty();
	}

	/** Check if a character is displayable */
	static private boolean isDisplayable(char c) {
		return isDisplayableBlock(c) &&
		      !Character.isISOControl(c);
	}

	/** Check if a character is in a displayable unicode block */
	static private boolean isDisplayableBlock(char c) {
		Character.UnicodeBlock b = Character.UnicodeBlock.of(c);
		return (b != null) && (b != Character.UnicodeBlock.SPECIALS);
	}

	/** Check if all characters in a string are displayable */
	static public boolean isDisplayable(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!isDisplayable(s.charAt(i)))
				return false;
		}
		return true;
	}

	/** Split a camel-case string into words.
	 * @param s String in camel case, e.g. 'blaBlaBla' or null.
	 * @return Space separated words, e.g. 'bla Bla Bla' or null */
	static public String splitCamel(String s) {
		String regex = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";
		if (s != null) {
			StringBuilder sb = new StringBuilder();
			for (String w : s.split(regex))
				sb.append(w).append(" ");
			return sb.toString().trim();
		}
		return null;
	}

	/** Extract string bounded by a specified prefix and postfix.
	 *  @param str String containing: prefix + middle + postfix. 
	 *  @param pre Prefix string, may be empty.
	 *  @param pos Postfix string, may be empty.
	 *  @return If str contains both the pre and postfix, the string 
	 *  		between them is returned, else the empty string. */
	static public String extractMiddle(String str, String pre, String pos){
		str = safe(str);
		if (!str.isEmpty() && str.startsWith(safe(pre)) && 
			str.endsWith(safe(pos)))
		{
			final int si = pre.length(); 
			final int ei = str.lastIndexOf(pos);
			return str.substring(si, ei);
		}
		return ""; 
	}  

	/** Return non-null stringRemove CR, LF, tabs and excess whitespace
	 * @param str String that may be null
	 * @return Argument with CR, LF tabs and extra spaces removed */
	static public String safe(String str) {
		return (str != null ? str : "");
	}
}
