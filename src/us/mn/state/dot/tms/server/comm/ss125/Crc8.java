/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

/**
 * A simple CRC-8 calculator.
 *
 * @author Douglas Lau
 */
public class Crc8 {

	/** Polynomial for CRC */
	static private final int POLYNOMIAL = 0x1c;

	/** Look-up table for CRC calculations */
	private final byte[] table = new byte[256];

	/** Initialize the lookup table */
	public Crc8() {
		for(int i = 0; i < table.length; i++) {
			int v = i;
		        for(int j = 0; j < 8; j++) {
				if((v & 0x80) != 0)
					v = (v << 1) ^ POLYNOMIAL;
				else
					v = v << 1;
			}
			table[i] = (byte)v;
		}
	}

	/** Calculate the CRC-8 of a buffer.
	 * @param buffer Buffer to be checked.
	 * @return CRC-8 of the buffer. */
	public int calculate(byte[] buffer, int n_bytes) {
		int crc = 0;
		for (int i = 0; i < n_bytes; i++)
			crc = table[(crc ^ buffer[i]) & 0xFF];
		return crc & 0xFF;
	}
}
