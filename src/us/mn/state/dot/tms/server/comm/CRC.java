/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

/**
 * A generic cyclic redundancy check (CRC) calculator.
 * It uses the Rocksoft parameterized model.
 * FIXME: does not work for width less than 8 or greater than 24.
 *
 * @author Douglas Lau
 */
public class CRC {

	/** Make a bit mask for a specified width */
	static private int make_mask(int width) {
		int mask = 0;
		for (int i = 0; i < width; i++)
			mask |= 1 << i;
		return mask;
	}

	/** Reflect the bits in a value */
	static private int do_reflect(int v, int n_bits) {
		int res = 0;
		for (int b = 0; b < n_bits; b++) {
			if ((v & 1) != 0)
				res |= 1 << (n_bits - b - 1);
			v >>= 1;
		}
		return res;
	}

	/** Bit width (between 8 and 32) */
	private final int width;

	/** Bit mask */
	private final int mask;

	/** Polynomial bit pattern */
	private final int polynomial;

	/** Seed (initial) CRC value */
	public final int seed;

	/** Reflect data bytes */
	private final boolean reflect;

	/** Reflect output */
	private final boolean ref_out;

	/** XOR result value */
	private final int xor_result;

	/** Look-up table */
	private final int[] table;

	/** Create a new CRC calculator.
	 * @param w Bit width (between 8 and 32).
	 * @param p Polynomial bit pattern.
	 * @param s Seed value.
	 * @param r Flag to reflect data.
	 * @param r Flag to reflect output.
	 * @param x Result XOR value. */
	public CRC(int w, int p, int s, boolean r, boolean ro, int x) {
		width = w;
		mask = make_mask(w);
		polynomial = p;
		seed = s;
		reflect = r;
		ref_out = ro;
		xor_result = x;
		table = createTable();
	}

	/** Create a new CRC calculator.
	 * @param w Bit width (between 8 and 32).
	 * @param p Polynomial bit pattern.
	 * @param s Seed value.
	 * @param r Flag to reflect data.
	 * @param x Result XOR value. */
	public CRC(int w, int p, int s, boolean r, int x) {
		this(w, p, s, r, false, x);
	}

	/** Create a new CRC calculator.
	 * @param w Bit width (between 8 and 32).
	 * @param p Polynomial bit pattern.
	 * @param s Seed value.
	 * @param r Flag to reflect data. */
	public CRC(int w, int p, int s, boolean r) {
		this(w, p, s, r, false, 0);
	}

	/** Create lookup-table */
	private int[] createTable() {
		int[] tbl = new int[256];
		for (int i = 0; i < 256; i++)
			tbl[i] = calculateTableValue(i);
		return tbl;
	}

	/** Calculate a table value */
	private int calculateTableValue(int i) {
		int top_bit = 1 << (width - 1);
		int ri = (reflect) ? do_reflect(i, 8) : i;
		int v = ri << (width - 8);
		for (int b = 0; b < 8; b++) {
			if ((v & top_bit) != 0)
				v = (v << 1) ^ polynomial;
			else
				v = (v << 1);
		}
		return (reflect) ? do_reflect(v, width) : v;
	}

	/** Calculate CRC from previous state.
	 * @param v Previous CRC state.
	 * @param d Byte of data.
	 * @return New CRC state. */
	public int step(int v, int d) {
		if (reflect) {
			int j = d ^ v;
			return (v >> 8) ^ table[j & 0xFF];
		} else {
			int j = d ^ (v >> (width - 8));
			return (v << 8) ^ table[j & 0xFF];
		}
	}

	/** Calculate the CRC result from final state.
	 * @param v Final CRC state.
	 * @return CRC result. */
	public int result(int v) {
		if (ref_out)
			v = do_reflect(v, width);
		return (v ^ xor_result) & mask;
	}

	/** Calculate the CRC for an array of data.
	 * @param data Array of data.
	 * @param n_bytes Number of bytes to check.
	 * @return CRC of data. */
	public int calculate(byte[] data, int n_bytes) {
		int v = seed;
		for (int i = 0; i < n_bytes; i++)
			v = step(v, data[i]);
		return result(v);
	}

	/** Calculate the checksum for an array of data */
	public int calculate(byte[] data) {
		return calculate(data, data.length);
	}
}
