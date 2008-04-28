/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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



package us.mn.state.dot.tms.comm.dmslite;

/**
 * Static String methods, provides static string convenience methods.
 *
 * @author Michael Darter
 */
public class SString {

    /**
     * Constructor. Should never be called.
     */
    private SString() {
        assert false : "should never get here.";
    }

    /**
     *  Given a filled field and string, return a string
     *  containing the field with the string right justified.
     *  e.g. ("0000","XY") returns "00XY".
     */
    static public String toRightField(String f, String s) {
        if (!((f != null) && (s != null))) {
            throw new IllegalArgumentException("SString.toRightField: arg f or s is null.");
        }

        if (!(f.length() >= s.length())) {
            throw new IllegalArgumentException("SString.toRightField: arg length problem:" + f + "," + s);
        }

        int    end = f.length() - s.length();
        String ret = f.substring(0, end) + s;

        return (ret);
    }

    /**
     *  test methods.
     */
    static public boolean test() {
        boolean ok = true;

        // toRightField
        // ok=ok && new String("").compareTo(SString.toRightField(null,""))==0;
        // ok=ok && new String("").compareTo(SString.toRightField("",null))==0;
        // ok=ok && new String("").compareTo(SString.toRightField(null,null))==0;
        ok = ok && (new String("").compareTo(SString.toRightField("", "")) == 0);
        ok = ok && (new String("1234a").compareTo(SString.toRightField("12345", "a")) == 0);
        ok = ok && (new String("1abcd").compareTo(SString.toRightField("12345", "abcd")) == 0);
        ok = ok && (new String("12345").compareTo(SString.toRightField("12345", "")) == 0);
        ok = ok && (new String("abcdef").compareTo(SString.toRightField("123456", "abcdef")) == 0);

        // ok=ok && new String("12345").compareTo(SString.toRightField("12345","abcdef"))==0;

        // removeEnclosingQuotes
        ok = ok && (new String("abcd").compareTo(SString.removeEnclosingQuotes("abcd")) == 0);
        ok = ok && (new String("abcd").compareTo(SString.removeEnclosingQuotes("\"abcd\"")) == 0);
        ok = ok && (new String("").compareTo(SString.removeEnclosingQuotes("")) == 0);
        ok = ok && (null == SString.removeEnclosingQuotes(null));
        ok = ok && (new String("\"abcd\" ").compareTo(SString.removeEnclosingQuotes("\"abcd\" ")) == 0);
        ok = ok && (new String("x").compareTo(SString.removeEnclosingQuotes("\"x\"")) == 0);

        return (ok);
    }

    /**
     *   return a hexstring given an integer. This method is like the Java
     *   method but converts the string to upper case.
     */
    static public String toHexString(int i) {
        String hex = Integer.toHexString(i);

        hex = hex.toUpperCase();

        return (hex);
    }

    /**
     *  Return a string with the enclosing double quotes removed.
     *  This method assumes the first and last chars are \" and
     *  if not the string is returned unmodified.
     */
    static public String removeEnclosingQuotes(String s) {
        if (s == null) {
            return (null);
        }

        if ((s.length() >= 2) && (s.charAt(0) == '\"') && (s.charAt(s.length() - 1) == '\"')) {
            return (s.substring(1, s.length() - 1));
        }

        return (s);
    }

    /**
     *      convert string to int.
     */
    public static int stringToInt(String s) {
        if (s == null) {
            return (0);
        }

        int i = 0;

        try {
            i = Integer.parseInt(s);
        } catch (Exception e) {}

        return (i);
    }

    /**
     *  convert string to double.
     */
    public static double stringToDouble(String s) {
        if (s == null) {
            return (0);
        }

        double d = 0;

        try {
            d = Double.parseDouble(s);
        } catch (Exception e) {}

        return (d);
    }

    /**
     *  convert int to string.
     */
    public static String intToString(int i) {
        String s = String.valueOf(i);

        return (s);
    }

    /**
     *  convert int to string.
     */
    public static String longToString(long i) {
        String s = String.valueOf(i);

        return (s);
    }

    /**
     *  Convert int to string with the specified number
     *  of digits, prefixing with zeros as necessary.
     *  e.g. (4,2) returns '04', (666,2) returns 666.
     */
    public static String intToString(int i, int numdigs) {
        String s             = String.valueOf(i);
        int    numzerostoadd = numdigs - s.length();

        if (numzerostoadd > 0) {
            for (int j = 0; j < numzerostoadd; ++j) {
                s = "0" + s;
            }
        }

        return (s);
    }
}
