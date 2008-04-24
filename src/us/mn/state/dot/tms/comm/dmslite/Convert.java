/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

//~--- JDK imports ------------------------------------------------------------

import java.lang.IllegalArgumentException;
import java.lang.StackTraceElement;

import java.util.Arrays;

/**
 * The convert class includes type conversion and formatting operations
 *
 * @author      Stephen Donecker
 * @company University of California, Davis
 * @created     February 15, 2008
 *
 */
public class Convert {

    /** constructor */
    private Convert() {}

    /**
     *  test methods in this class.
     */
    static public boolean test() {
        boolean ok = true;

        // byteArrayToCharArray
        // log.fine("Testing byteArrayToCharArray");
        {
            byte[] ba = new byte[] { 32, 32, 32 };
            char[] ca = byteArrayToCharArray(ba);

            ok = ok && (ca.length == 3);

            for (int i = 0; i < ca.length; ++i) {
                ok = ok && (ca[i] == 32);
            }
        }

        // byteArrayToCharArray
        // log.fine("Testing byteArrayToCharArray");
        {
            byte[] ba = new byte[] { 32, 32, 32, 32, 32 };
            char[] ca = byteArrayToCharArray(2, ba);

            ok = ok && (ca.length == 2);

            for (int i = 0; i < ca.length; ++i) {
                ok = ok && (ca[i] == 32);
            }
        }

        // isEven
        // log.fine("Testing isEven");
        ok = ok && isEven(0);
        ok = ok && isEven(2);
        ok = ok && isEven(-2);
        ok = ok &&!isEven(1);
        ok = ok &&!isEven(3);

        // charToByte
        // log.fine("Testing charToByte");
        ok = ok && (charToByte('0') == 0);
        ok = ok && (charToByte('1') == 1);
        ok = ok && (charToByte('2') == 2);
        ok = ok && (charToByte('3') == 3);
        ok = ok && (charToByte('4') == 4);
        ok = ok && (charToByte('5') == 5);
        ok = ok && (charToByte('a') == 10);
        ok = ok && (charToByte('B') == 11);
        ok = ok && (charToByte('c') == 12);
        ok = ok && (charToByte('D') == 13);
        ok = ok && (charToByte('e') == 14);
        ok = ok && (charToByte('F') == 15);

        // hexToByte
        // log.fine("Testing hexToByte");
        ok = ok && (hexToByte('1', '1') == 17);
        ok = ok && (hexToByte('f', 'f') == 255);
        ok = ok && (hexToByte('0', '0') == 0);
        ok = ok && (hexToByte('1', '0') == 16);
        ok = ok && (hexToByte('0', '1') == 1);
        ok = ok && (hexToByte('E', 'E') == 238);

        // toHexString
        // log.fine("Testing toHexString");
        ok = ok && (Convert.toHexString((byte) 0).compareToIgnoreCase("00") == 0);
        ok = ok && (Convert.toHexString((byte) 1).compareToIgnoreCase("01") == 0);
        ok = ok && (Convert.toHexString((byte) 10).compareToIgnoreCase("0A") == 0);
        ok = ok && (Convert.toHexString((byte) 11).compareToIgnoreCase("0B") == 0);
        ok = ok && (Convert.toHexString((byte) 12).compareToIgnoreCase("0C") == 0);
        ok = ok && (Convert.toHexString((byte) 13).compareToIgnoreCase("0D") == 0);
        ok = ok && (Convert.toHexString((byte) 14).compareToIgnoreCase("0E") == 0);
        ok = ok && (Convert.toHexString((byte) 15).compareToIgnoreCase("0F") == 0);
        ok = ok && (Convert.toHexString((byte) 16).compareToIgnoreCase("10") == 0);
        ok = ok && (Convert.toHexString((byte) 254).compareToIgnoreCase("FE") == 0);
        ok = ok && (Convert.toHexString((byte) 255).compareToIgnoreCase("FF") == 0);

        // appendToHexString
        // log.fine("Testing appendToHexString");
        {
            StringBuilder sb = new StringBuilder(0);

            sb = appendToHexString(sb, (byte) 255);
            ok = ok && (sb.length() == 2);
            ok = ok && (sb.toString().compareToIgnoreCase("FF") == 0);
            sb = appendToHexString(sb, (byte) 254);
            ok = ok && (sb.length() == 4);
            ok = ok && (sb.toString().compareToIgnoreCase("FFFE") == 0);
        }

        // hexStringToByteArray
        // log.fine("Testing hexStringToByteArray");
        {
            byte[] a;

            a  = Convert.hexStringToByteArray("0001090a0A0b0fFFfe");
            ok = ok && (a.length == 9);
            ok = ok && (a[0] == 0);
            ok = ok && (a[1] == 1);
            ok = ok && (a[2] == 9);
            ok = ok && (a[3] == 10);
            ok = ok && (a[4] == 10);
            ok = ok && (a[5] == 11);
            ok = ok && (a[6] == 15);
            ok = ok && (a[7] == (byte) 255);
            ok = ok && (a[8] == (byte) 254);
        }

        // reverseByte
        // log.fine("Testing reverseByte");
        {
            ok = ok && (reverseByte((byte) 0) == (byte) 0);
            ok = ok && (reverseByte((byte) 1) == (byte) 128);
            ok = ok && (reverseByte((byte) 2) == 64);
            ok = ok && (reverseByte((byte) 4) == 32);
            ok = ok && (reverseByte((byte) 8) == 16);
            ok = ok && (reverseByte((byte) 16) == 8);
            ok = ok && (reverseByte((byte) 32) == 4);
            ok = ok && (reverseByte((byte) 64) == 2);
            ok = ok && (reverseByte((byte) 128) == 1);
            ok = ok && (reverseByte((byte) 255) == (byte) 255);
        }

        // log.info("Common.Convert.test() ok? "+ok);
        return (ok);
    }

    /** Convert integer to byte array of specified length */
    public static byte[] toByteArray(int value, int length) {

        // check preconditions
        if ((length > 4) || (length < 1)) {
            throw new IllegalArgumentException();
        }

        // convert integer to byte array
        byte[] anArray = new byte[length];

        for (int i = 0; i < anArray.length; i++) {
            anArray[i] = (byte) ((value >> ((anArray.length - 1 - i) * 8)) & 0xFF);
        }

        return anArray;
    }

    /** Convert byte array of specified length to integer */
    public static int toInteger(byte[] anArray) {

        // check preconditions
        if (anArray.length > 4) {
            throw new IllegalArgumentException();
        }

        // not null
        // convert byte array to integer
        int anInteger = 0;

        for (int i = 0; i < anArray.length; i++) {
            anInteger += ((anArray[i] & 0xFF) << ((anArray.length - 1 - i) * 8));
        }

        return anInteger;
    }

    /**
     * Convert byte array to a string of hex values with whitespace delimiter
     *            e.g. {0,1,2,3} to "00 01 02 03"
     */
    public static String toHexString(byte[] anArray) {
        StackTraceElement stack = new Throwable().fillInStackTrace().getStackTrace()[1];

        if (!Verify.isNull(anArray)) {
            StringBuffer output = new StringBuffer(anArray.length * 2 + 2);

            if (anArray.length > 0) {
                for (int i = 0; i < anArray.length; i++) {
                    output.append(Integer.toHexString((byte) ((anArray[i] >> 4) & 0x0F)));
                    output.append(Integer.toHexString((byte) (anArray[i] & 0x0F)));
                }
            }

            return output.toString().toUpperCase();
        } else {

            // log.finest(stack,"The byte array is null");
            return "";
        }
    }

    /** Convert byte array to a string of hex values with specified deliminator */
    public static String toHexString(byte[] anArray, char aDelimiter) {
        StackTraceElement stack = new Throwable().fillInStackTrace().getStackTrace()[1];

        if (!Verify.isNull(anArray)) {
            StringBuffer output = new StringBuffer(anArray.length * 2 + 2);

            if (anArray.length > 0) {
                for (int i = 0; i < anArray.length; i++) {
                    output.append(Integer.toHexString((byte) ((anArray[i] >> 4) & 0x0F)));
                    output.append(Integer.toHexString((byte) (anArray[i] & 0x0F)));
                    output.append(aDelimiter);
                }

                output.deleteCharAt(output.length() - 1);
            }

            return output.toString().toUpperCase();
        } else {

            // log.finest(stack,"The byte array is null");
            return "";
        }
    }

    /** return true if the int is even else false */
    public static boolean isEven(int n) {
        return (n % 2 == 0);
    }

    /**
     *  Convert a string in hex format to a byte array.
     * Note, because java has no unsigned, ff will
     * convert to 255 which is -1.
     *     e.g. "000102ff" converts to {0,1,2,255}
     */
    public static byte[] hexStringToByteArray(String hs) {

        // sanity checks
        if ((hs == null) || (hs.length() <= 0) ||!Convert.isEven(hs.length())) {
            throw new IllegalArgumentException("bogus arg to hexStringToByteArray");
        }

        int    len = hs.length() / 2;
        byte[] ba  = new byte[len];

        for (int i = 0, j = 0; i < hs.length(); i += 2, j++) {
            char c1 = hs.charAt(i);
            char c2 = hs.charAt(i + 1);

            ba[j] = (byte) Convert.hexToByte(c1, c2);
        }

        return (ba);
    }

    /**
     *  Convert two hex chars to a single byte.
     *  e.g. 'f' 'f' returns 255.
     */
    public static int hexToByte(char ms, char ls) {
        int msb = Convert.charToByte(ms);
        int lsb = Convert.charToByte(ls);
        int b   = (int) ((msb << 4) | lsb);

        assert(b >= 0) && (b < 256) : "bogus return value in hexToByte";

        return (b);
    }

    /**
     *  Convert ascii hex char to a single binary byte.
     *      e.g. 'f' returns 15.
     */
    public static byte charToByte(char c) {
        byte b = 0;

        if ((c >= '0') && (c <= '9')) {
            b = (byte) ((byte) c - (byte) 48);
        } else if ((c == 'a') || (c == 'A')) {
            b = 10;
        } else if ((c == 'b') || (c == 'B')) {
            b = 11;
        } else if ((c == 'c') || (c == 'C')) {
            b = 12;
        } else if ((c == 'd') || (c == 'D')) {
            b = 13;
        } else if ((c == 'e') || (c == 'E')) {
            b = 14;
        } else if ((c == 'f') || (c == 'F')) {
            b = 15;
        }

        return (b);
    }

    /**
     *  Convert a byte to a string containing a hex value, and append
     * to the specified StringBuilder.
     *     e.g. 1 converts to "01"
     */
    public static StringBuilder appendToHexString(StringBuilder sb, byte aByte) {
        sb.append(Integer.toHexString((byte) ((aByte >> 4) & 0x0F)));
        sb.append(Integer.toHexString((byte) (aByte & 0x0F)));

        return (sb);
    }

    /**
     *  Convert a byte to a string containing a hex value
     *     e.g. 1 converts to "01"
     */
    public static String toHexString(byte aByte) {
        StringBuffer output = new StringBuffer(2);

        output.append(Integer.toHexString((byte) ((aByte >> 4) & 0x0F)));
        output.append(Integer.toHexString((byte) (aByte & 0x0F)));

        return output.toString().toUpperCase();
    }

    /**
     * convert String to int.
     */
    public static int stringToInt(String s) {
        if (s == null) {
            return (0);
        }

        int i = 0;

        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException e) {}

        return (i);
    }

    /**
     * convert byte[] to char[] using assumed encoding.
     *
     * @returns May return null.
     */
    public static char[] byteArrayToCharArray(int len, byte[] ba) {
        char[] ca;

        try {
            ca = new String(ba, 0, len, "ISO-8859-1").toCharArray();
        } catch (Exception UnsupportedEncodingException) {
            ca = null;
        }

        return (ca);
    }

    /**
     * Resize an array to the specified number of elements.
     *
     * @returns resized array.
     */
    public static char[] byteArrayToCharArray(byte[] ba) {
        return (Convert.byteArrayToCharArray(ba.length, ba));
    }

    /**
     * Reverse the bit order of a byte.
     *
     * @returns reversed bit order byte.
     */
    public static byte reverseByte(byte in) {
        byte out = 0;
        byte i   = 0;

        for (; i < 8; i++) {
            int t = (in & 1) + (out << 1);

            out = (byte) (t & 0xFF);
            in  >>= 1;
        }

        return (out);
    }

    /**
     * Reverse the bit order of each byte in a byte array.
     */
    public static byte[] reverseByte(byte[] in) {
        if (in == null) {
            throw new IllegalArgumentException("arg null in reverseByte()");
        }

        byte[] ret = new byte[in.length];

        for (int i = 0; i < in.length; ++i) {
            ret[i] = Convert.reverseByte(in[i]);
        }

        return (ret);
    }
}
