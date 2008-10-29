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
 * HexString class, provides both convienence methods and 
 * an instance of a HexString.
 * @author Michael Darter
 */
final public class HexString
{
	/** hex string */
	protected String m_hexstring = "";

	/** Constructor */
	public HexString(String s) {
		m_hexstring = s;
	}

	/** Constructor */
	public HexString(byte[] a) {
		m_hexstring = toHexString(a);
	}

	/** return the hex string */
	public String toString() {
		return m_hexstring;
	}

	/** append another HexString */
	public HexString append(HexString h) {
		m_hexstring = m_hexstring + h.m_hexstring;
		return this;
	}

	/** return the hex string */
	public int length() {
		return m_hexstring.length();
	}

	/** return the hex string as a byte array */
	public byte[] toByteArray() {
		return hexStringToByteArray(m_hexstring);
	}

	/** test methods */
	static public boolean test() {
		boolean ok = true;

		// charToByte
		System.err.println("Testing charToByte");
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
		System.err.println("Testing hexToByte");
		ok = ok && (hexToByte('1', '1') == 17);
		ok = ok && (hexToByte('f', 'f') == 255);
		ok = ok && (hexToByte('0', '0') == 0);
		ok = ok && (hexToByte('1', '0') == 16);
		ok = ok && (hexToByte('0', '1') == 1);
		ok = ok && (hexToByte('E', 'E') == 238);

		// isEven
		System.err.println("Testing isEven");
		ok = ok && isEven(0);
		ok = ok && isEven(2);
		ok = ok && isEven(-2);
		ok = ok &&!isEven(1);
		ok = ok &&!isEven(3);

		// toHexString
		System.err.println("Testing toHexString");
		ok = ok && (toHexString((byte) 0).compareToIgnoreCase(
			"00") == 0);
		ok = ok && (toHexString((byte) 1).compareToIgnoreCase(
			"01") == 0);
		ok = ok && (toHexString((byte) 10).compareToIgnoreCase(
			"0A") == 0);
		ok = ok && (toHexString((byte) 11).compareToIgnoreCase(
			"0B") == 0);
		ok = ok && (toHexString((byte) 12).compareToIgnoreCase(
			"0C") == 0);
		ok = ok && (toHexString((byte) 13).compareToIgnoreCase(
			"0D") == 0);
		ok = ok && (toHexString((byte) 14).compareToIgnoreCase(
			"0E") == 0);
		ok = ok && (toHexString((byte) 15).compareToIgnoreCase(
			"0F") == 0);
		ok = ok && (toHexString((byte) 16).compareToIgnoreCase(
			"10") == 0);
		ok = ok && (toHexString((byte) 254).compareToIgnoreCase(
			"FE") == 0);
		ok = ok && (toHexString((byte) 255).compareToIgnoreCase(
			"FF") == 0);

		// appendToHexString
		System.err.println("Testing appendToHexString");
		{
			StringBuilder sb = new StringBuilder(0);

			sb = appendToHexString(sb, (byte) 255);
			ok = ok && (sb.length() == 2);
			ok = ok && (sb.toString().compareToIgnoreCase("FF")
				    == 0);
			sb = appendToHexString(sb, (byte) 254);
			ok = ok && (sb.length() == 4);
			ok = ok && (sb.toString().compareToIgnoreCase("FFFE")
				    == 0);
		}

		// hexStringToByteArray
		System.err.println("Testing hexStringToByteArray");
		{
			byte[] a;

			a = hexStringToByteArray("0001090a0A0b0fFFfe");
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

		return ok;
	}

	/**
	 * Convert byte array to a string of hex values with whitespace delimiter
	 * e.g. {0,1,2,3} to "00 01 02 03"
	 */
	public static String toHexString(byte[] anArray) {
		if(anArray == null)
			return "";
		StringBuffer output = new StringBuffer(anArray.length
					      * 2 + 2);
		if(anArray.length > 0) {
			for(int i = 0; i < anArray.length; i++) {
				output.append(
				    Integer.toHexString(
					    (byte) ((anArray[i] >> 4)
						    & 0x0F)));
				output.append(
				    Integer.toHexString(
					    (byte) (anArray[i]
						    & 0x0F)));
			}
		}
		return output.toString().toUpperCase();
	}

	/** Convert byte array to a string of hex values with specified delimiter */
	public static String toHexString(byte[] anArray, char aDelimiter) {
		if(anArray == null)
			return "";
		StringBuffer output = new StringBuffer(anArray.length
					      * 2 + 2);
		if(anArray.length > 0) {
			for(int i = 0; i < anArray.length; i++) {
				output.append(
				    Integer.toHexString(
					    (byte) ((anArray[i] >> 4)
						    & 0x0F)));
				output.append(
				    Integer.toHexString(
					    (byte) (anArray[i]
						    & 0x0F)));
				output.append(aDelimiter);
			}
			output.deleteCharAt(output.length() - 1);
		}
		return output.toString().toUpperCase();
	}

	/**
	 * Convert a string in hex format to a byte array. Note, because java has no
	 * unsigned, ff will convert to 255 which is -1. e.g. "000102ff" converts to
	 * {0,1,2,255}
	 */
	public static byte[] hexStringToByteArray(String hs) {

		// sanity checks
		if((hs == null) || (hs.length() <= 0)
			||!isEven(hs.length())) {
			throw new IllegalArgumentException(
			    "bogus arg to hexStringToByteArray");
		}

		int len = hs.length() / 2;
		byte[] ba = new byte[len];

		for(int i = 0, j = 0; i < hs.length(); i += 2, j++) {
			char c1 = hs.charAt(i);
			char c2 = hs.charAt(i + 1);

			ba[j] = (byte) hexToByte(c1, c2);
		}

		return (ba);
	}

	/**
	 * Convert two hex chars to a single byte. e.g. 'f' 'f' returns 255.
	 */
	public static int hexToByte(char ms, char ls) {
		int msb = charToByte(ms);
		int lsb = charToByte(ls);
		int b = (int) ((msb << 4) | lsb);

		assert(b >= 0) && (b < 256) : "bogus return value in hexToByte";

		return (b);
	}

	/**
	 * Convert ascii hex char to a single binary byte. e.g. 'f' returns 15.
	 */
	public static byte charToByte(char c) {
		byte b = 0;

		if((c >= '0') && (c <= '9')) {
			b = (byte) ((byte) c - (byte) 48);
		} else if((c == 'a') || (c == 'A')) {
			b = 10;
		} else if((c == 'b') || (c == 'B')) {
			b = 11;
		} else if((c == 'c') || (c == 'C')) {
			b = 12;
		} else if((c == 'd') || (c == 'D')) {
			b = 13;
		} else if((c == 'e') || (c == 'E')) {
			b = 14;
		} else if((c == 'f') || (c == 'F')) {
			b = 15;
		}

		return (b);
	}

	/**
	 * Convert a byte to a string containing a hex value, and append to the
	 * specified StringBuilder. e.g. 1 converts to "01"
	 */
	public static StringBuilder appendToHexString(StringBuilder sb,
		byte aByte) {
		sb.append(Integer.toHexString((byte) ((aByte >> 4) & 0x0F)));
		sb.append(Integer.toHexString((byte) (aByte & 0x0F)));

		return (sb);
	}

	/** Convert a byte to a string containing a hex value e.g. 1 converts to "01" */
	public static String toHexString(byte aByte) {
		StringBuffer output = new StringBuffer(2);

		output.append(Integer.toHexString((byte) ((aByte >> 4)
			& 0x0F)));
		output.append(Integer.toHexString((byte) (aByte & 0x0F)));

		return output.toString().toUpperCase();
	}


	/** return true if the int is even else false */
	private static boolean isEven(int n) {
		return (n % 2 == 0);
	}
}
