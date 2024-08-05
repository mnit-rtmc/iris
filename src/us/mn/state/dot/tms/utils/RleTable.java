/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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
 * RLE table for non-negative integers
 *
 * @author Douglas Lau
 */
public class RleTable {

	/** Buffer of encoded data */
	private final Pint64 data;

	/** Current value */
	private int value = -1;

	/** Repeat count */
	private int count = 0;

	/** Make new encoder */
	public RleTable() {
		data = new Pint64();
	}

	/** Make new decoder */
	public RleTable(String d) {
		data = new Pint64(d);
	}

	/** Get encoded data as a string */
	@Override
	public String toString() {
		flush();
		return data.toString();
	}

	/** Flush values */
	private void flush() {
		if (value >= 0) {
			// encode value + repeat count
			data.encode(value);
			data.encode(count);
			value = -1;
			count = 0;
		}
	}

	/** Encode one non-negative integer */
	public void encode(int v) {
		if (v < 0)
			throw new IllegalArgumentException();
		if (v == value)
			count++;
		else {
			flush();
			value = v;
		}
	}

	/** Decode one non-negative integer */
	public int decode() {
		if (value < 0) {
			value = data.decode();
			count = data.decode();
		}
		int v = value;
		count--;
		if (count < 0) {
			value = -1;
			count = 0;
		}
		return v;
	}
}
