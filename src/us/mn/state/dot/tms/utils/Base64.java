/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.UnmappableCharacterException;

/**
 * Simple static Base64 encoder/decoder.
 *
 * @author Douglas Lau
 */
public class Base64 {

	/** Line length for Base64 encoded data */
	static private final int LINE_LENGTH = 76;

	/** Code points used for Base64 encoding */
	static private final String CODE =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" +
		"0123456789+/";

	/** Get the character code of one 6-bit value */
	static private char getCode(int value) {
		return CODE.charAt(value & 0x3f);
	}

	/** Calculate the chunk value from source byte array */
	static private int chunk(byte[] src, int off) {
		return (src[off + 0] & 0xFF) << 16 |
		       (src[off + 1] & 0xFF) << 8 |
		       (src[off + 2] & 0xFF);
	}

	/** Encode bytes into a 4-character Base64 chunk */
	static private void encodeChunk(int src, int n_bytes,
		StringBuilder dest)
	{
		dest.append(getCode(src >> 18));
		dest.append(getCode(src >> 12));
		dest.append((n_bytes > 1) ? getCode(src >> 6) : '=');
		dest.append((n_bytes > 2) ? getCode(src) : '=');
	}

	/** Encode an array of bytes to a Base64 string */
	static public String encode(byte[] src) {
		int d = 0;
		int line = 0;
		StringBuilder dest = new StringBuilder();
		for (; d < src.length - 2; d += 3) {
			encodeChunk(chunk(src, d), 3, dest);
			line += 4;
			if (line == LINE_LENGTH) {
				dest.append('\n');
				line = 0;
			}
		}
		int rem = src.length - d;
		if (rem > 0) {
			byte[] c = new byte[3];
			System.arraycopy(src, d, c, 0, rem);
			encodeChunk(chunk(c, 0), rem, dest);
		}
		return dest.toString();
	}

	/** Code for equals-sign */
	static private final int EQUALS_SIGN = -1;

	/** Code for whitespace */
	static private final int WHITESPACE = -2;

	/** Code for unmapped characters */
	static private final int UNMAPPED = -3;

	/** Decode table used for Base64 decoding */
	static private final int[] DECODE = new int[0x80];
	static {
		for (int i = 0; i < DECODE.length; i++)
			DECODE[i] = UNMAPPED;
		for (int i = 0; i < CODE.length(); i++) {
			int c = CODE.charAt(i);
			DECODE[c] = i;
		}
		DECODE['='] = EQUALS_SIGN;
		DECODE[' '] = WHITESPACE;
		DECODE['\t'] = WHITESPACE;
		DECODE['\n'] = WHITESPACE;
		DECODE['\r'] = WHITESPACE;
	}

	/** Get the 6-bit value of one character coce */
	static private int getValue(char code) {
		if (code < 0 || code >= DECODE.length)
			return UNMAPPED;
		return DECODE[code];
	}

	/** Calculate a chunk array from an encoded string */
	static private byte[] chunk(StringBuilder src) throws IOException {
		int c = 0;
		int s = 0;
		int i = 1;
		for (; s < src.length(); s++) {
			int v = getValue(src.charAt(s));
			if (v == UNMAPPED)
				throw new UnmappableCharacterException(0);
			if (v >= 0) {
				c |= v << (24 - i * 6);
				if (i > 4)
					break;
				i++;
			}
		}
		src.delete(0, s);
		int l = Math.max(0, i - 2);
		byte[] b = new byte[l];
		if (l > 0)
			b[0] = (byte)((c >> 16) & 0xFF);
		if (l > 1)
			b[1] = (byte)((c >> 8) & 0xFF);
		if (l > 2)
			b[2] = (byte)(c & 0xFF);
		return b;
	}

	/** Decode a Base64 string to an array of bytes */
	static public byte[] decode(String s) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StringBuilder b = new StringBuilder(s);
		while (b.length() > 0) {
			byte[] c = chunk(b);
			bos.write(c, 0, c.length);
		}
		return bos.toByteArray();
	}

	/** Calculate the number of characters needed to encode data.
	 * @param n_bits Number of bits to encode.
	 * @return Number of characters for Base64 encoding. */
	static public int numCharacters(int n_bits) {
		int n_bytes = (n_bits + 7) / 8;
		int n_chunks = (n_bytes + 2) / 3;
		return n_chunks * 4;
	}

	/** Don't allow instantiation */
	private Base64() { }
}
