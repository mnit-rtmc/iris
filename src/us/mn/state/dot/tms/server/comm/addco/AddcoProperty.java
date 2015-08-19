/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.addco;

import java.io.InputStream;
import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.CRC;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Addco property.
 *
 * @author Douglas Lau
 */
abstract public class AddcoProperty extends ControllerProperty {

	/** Charset name for ASCII */
	static private final String ASCII = "US-ASCII";

	/** CRC-16 algorithm */
	static private final CRC crc16 = new CRC(16, 0x8005, 0xFFFF, true);

	/** Parse an ASCII value */
	static protected String parseAscii(byte[] buf, int pos, int n_len)
		throws IOException
	{
		return new String(buf, pos, n_len, ASCII);
	}

	/** Current CRC value */
	private int crc;

	/** Process the CRC for a buffer */
	private void processCrc(byte[] buf) {
		for (byte b: buf)
			crc = crc16.step(crc, b);
	}

	/** Decode header of response */
	protected int decodeHead(InputStream is, MsgCode mc) throws IOException{
		crc = crc16.seed;
		byte[] bc = recvResponse(is, 1);
		processCrc(bc);
		if (bc[0] != mc.code)
			throw new ParsingException("MSG CODE: " + bc[0]);
		if (mc == MsgCode.NORMAL) {
			byte[] len = recvResponse(is, 2);
			processCrc(len);
			return parse16le(len, 0);
		} else
			return 0;
	}

	/** Decode body of response */
	protected byte[] decodeBody(InputStream is, int address, int len)
		throws IOException
	{
		byte[] body = recvResponse(is, len - 5); // - header / fcs
		processCrc(body);
		int addr = parse16le(body, 0);
		if (addr != (address & 0xFFFF))
			throw new ParsingException("ADDRESS: " + addr);
		byte[] b_fcs = recvResponse(is, 2);
		int fcs = parse16(b_fcs, 0);	// swap bytes; not LE
		if (fcs != crc16.result(crc))
			throw new ChecksumException(b_fcs);
		return body;
	}

	/** Check response command */
	protected void checkCommand(byte[] body, String cmnd) throws IOException
	{
		assert body.length >= 4;
		assert cmnd.length() == 2;
		String c = parseAscii(body, 2, 2);
		if (!c.equals(cmnd))
			throw new ParsingException("INVALID COMMAND: " + c);
	}
}
