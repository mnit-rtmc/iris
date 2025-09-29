/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024-2025  Minnesota Department of Transportation
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
 * Non-negative integer encoder/decoder with a custom base64-alphabet.
 *
 * @author Douglas Lau
 */
public class Pint64 {

	/** Code points used for encoding */
	static private final String CODE =
		"0123456789._ABCDEFGHIJKLMNOPQRST" +
		"UVWXYZabcdefghijklmnopqrstuvwxyz";

	/** Decode one character */
	static int decodeChar(int c) {
		if (c >= '0' && c <= '9')
			return c - '0';
		else if (c == ';' || c == '.')
			return 10;
		else if (c == '?' || c == '_')
			return 11;
		else if (c >= 'A' && c <= 'Z')
			return 12 + c - 'A';
		else if (c >= 'a' && c <= 'z')
			return 38 + c - 'a';
		else
			return -1;
	}

	/** Buffer of encoded data */
	private final StringBuilder data;

	/** Make new encoder */
	public Pint64() {
		data = new StringBuilder();
	}

	/** Make new decoder */
	public Pint64(String d) {
		data = new StringBuilder(d);
	}

	/** Get encoded data as a string */
	@Override
	public String toString() {
		return data.toString();
	}

	/** Encode one non-negative integer */
	public void encode(int value) {
		if (value < 0)
			throw new IllegalArgumentException();
		// Encode lower bits of integer, 5 at a time
		while (value > 0x1F) {
			int v = 0x20 | (value & 0x1F);
			data.append(CODE.charAt(v));
			value >>= 5;
		}
		// Encode high 5 bits
		data.append(CODE.charAt(value));
	}

	/** Decode the next character */
	private int nextChar() {
		int c = data.charAt(0);
		data.deleteCharAt(0);
		return decodeChar(c);
	}

	/** Decode one non-negative integer */
	public int decode() {
		int i;
		int value = 0;
		// Decode lower bits of integer, 5 at a time
		for (i = 0; i < 32; i += 5) {
			int v = nextChar();
			if (v < 0)
				return v;
			value |= (v & 0x1F) << i;
			if ((v & 0x20) == 0)
				break;
		}
		if (i >= 30)
			throw new IllegalArgumentException();
		return value;
	}
}
