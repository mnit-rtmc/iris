/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
 * Methods for converting byte arrays to and from hex string.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public final class HexString {

	/** Prevent instantiation */
	private HexString() { }

	/** Convert a byte to a string containing a hex value, and append to the
	 * specified StringBuilder. e.g. 1 converts to "01". */
	static private void appendHexString(StringBuilder sb, byte aByte) {
		sb.append(Integer.toHexString((aByte >> 4) & 0x0F));
		sb.append(Integer.toHexString(aByte & 0x0F));
	}

	/** Format a byte as a hex string.
	 * e.g. 1 converts to "01". */
	static public String format(byte aByte) {
		StringBuilder sb = new StringBuilder(2);
		appendHexString(sb, aByte);
		return sb.toString().toUpperCase();
	}

	 /** Format a byte array as a hex with no delimiter.
	  * e.g. {0,1,2,3} to "00010203". */
	static public String format(byte[] data) {
		StringBuilder sb = new StringBuilder();
		if(data != null) {
			for(int i = 0; i < data.length; i++)
				appendHexString(sb, data[i]);
		}
		return sb.toString().toUpperCase();
	}

	/** Format a byte array as a hex string with specified delimiter. */
	static public String format(byte[] data, char delim) {
		StringBuilder sb = new StringBuilder();
		if(data != null) {
			for(int i = 0; i < data.length; i++) {
				if(i > 0)
					sb.append(delim);
				appendHexString(sb, data[i]);
			}
		}
		return sb.toString().toUpperCase();
	}

	/** Parse a hex string to a byte array.
	 * @param hs Hex string to parse.
	 * @return byte array of parsed hex string.
	 * @throws NullPointerException if hs is null.
	 * @throws IllegalArgumentException if length is not even.
	 * @throws NumberFormatException if hex cannot be parsed. */
	static public byte[] parse(String hs) {
		if(!isEven(hs.length()))
			throw new IllegalArgumentException("Length not even");
		int len = hs.length() / 2;
		byte[] ba = new byte[len];
		for(int i = 0; i < len; i++) {
			int j = i * 2;
			String ss = hs.substring(j, j + 2);
			ba[i] = (byte)Integer.parseInt(ss, 16);
		}
		return ba;
	}

	/** return true if the int is even else false */
	static private boolean isEven(int n) {
		return n % 2 == 0;
	}
}
