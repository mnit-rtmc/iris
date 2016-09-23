/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
 * Methods for converting byte arrays to and from a hexadecimal string.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public final class HexString {

	/** Hexadecimal digits */
	static private final String DIGITS = "0123456789ABCDEF";

	/** Get one hexadecimal digit (upper case) */
	static private char digit(int v) {
		return DIGITS.charAt(v & 0x0F);
	}

	/** Append one byte to a hexadecimal string builder.
	 * @param sb String builder to append hexadecimal digits.
	 * @param b Byte to append to string. */
	static private void append(StringBuilder sb, byte b) {
		sb.append(digit(b >> 4));
		sb.append(digit(b >> 0));
	}

	/** Format a byte array as a hexadecimal string.
	 * @param data Array of bytes to format.
	 * @return Formatted hexadecimal string. */
	static public String format(byte[] data) {
		StringBuilder sb = new StringBuilder();
		if (data != null) {
			for (int i = 0; i < data.length; i++)
				append(sb, data[i]);
		}
		return sb.toString();
	}

	/** Format a byte array as a hexadecimal string.
	 * @param data Byte array.
	 * @param off Offset in array.
	 * @param len Number of bytes to format.
	 * @param delim Delimeter between each byte.
	 * @return Formatted hexadecimal string. */
	static public String format(byte[] data, int off, int len, char delim) {
		StringBuilder sb = new StringBuilder();
		if (data != null) {
			for (int i = off; i < off + len; i++) {
				if (sb.length() > 0)
					sb.append(delim);
				append(sb, data[i]);
			}
		}
		return sb.toString();
	}

	/** Format a byte array as a hexadecimal string.
	 * @param data Array of bytes to format.
	 * @param len Length of array.
	 * @param delim Delimeter between each byte.
	 * @return Formatted hexadecimal string. */
	static public String format(byte[] data, int len, char delim) {
		return format(data, 0, len, delim);
	}

	/** Format a byte array as a hexadecimal string.
	 * @param data Array of bytes to format.
	 * @param delim Delimeter between each byte.
	 * @return Formatted hexadecimal string. */
	static public String format(byte[] data, char delim) {
		return format(data, 0, data.length, delim);
	}

	/** Format an integer as a hexadecimal string.
	 * @param v Value to format.
	 * @param d Number of digits to format.
	 * @return Formatted hexadecimal string. */
	static public String format(int v, int d) {
		StringBuilder sb = new StringBuilder();
		for (int i = d - 1; i >= 0; i--)
			sb.append(digit(v >> (i * 4)));
		return sb.toString();
	}

	/** Parse a hex string to a byte array.
	 * @param hs Hex string to parse.
	 * @return byte array of parsed hex string.
	 * @throws NullPointerException if hs is null.
	 * @throws IllegalArgumentException if length is not even.
	 * @throws NumberFormatException if hex cannot be parsed. */
	static public byte[] parse(String hs) {
		if (!isEven(hs.length()))
			throw new IllegalArgumentException("Length not even");
		int len = hs.length() / 2;
		byte[] ba = new byte[len];
		for (int i = 0; i < len; i++) {
			int j = i * 2;
			String ss = hs.substring(j, j + 2);
			ba[i] = (byte)Integer.parseInt(ss, 16);
		}
		return ba;
	}

	/** Return true if the int is even else false */
	static private boolean isEven(int n) {
		return (n % 2) == 0;
	}

	/** Prevent instantiation */
	private HexString() { }
}
