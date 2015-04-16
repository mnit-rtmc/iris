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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.OutputStream;
import us.mn.state.dot.tms.server.comm.CRC;

/**
 * This is an implementation of the CRC-16 algorithm described in ISO/IEC
 * standard 3309.
 *
 * @author Douglas Lau
 */
public class CRCStream extends OutputStream {

	/** CRC-16 algorithm */
	static private final CRC crc16 = new CRC(16, 0x1021, 0xFFFF, true,
		0xFFFF);

	/** Current CRC value */
	private int crc = crc16.seed;

	/** Write a byte to the CRC output stream */
	@Override
	public void write(int b) {
		crc = crc16.step(crc, b);
	}

	/** Get the calculated CRC */
	public int getCrc() {
		return crc16.result(crc);
	}

	/** Get the calculated CRC with bytes swapped */
	public int getCrcSwapped() {
		int v = getCrc();
		return ((v & 0xFF) << 8) | ((v >> 8) & 0xFF);
	}
}
