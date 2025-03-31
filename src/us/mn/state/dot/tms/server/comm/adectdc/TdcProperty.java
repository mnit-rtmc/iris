/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.adectdc;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.InvalidAddressException;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * ADEC TDC property.
 *
 * @author Douglas Lau
 */
abstract public class TdcProperty extends ControllerProperty {

	/** Frame type identifier values */
	static private final byte FRAME_ID_SINGLE = (byte) 0xE5;
	static private final byte FRAME_ID_SHORT = (byte) 0x10;
	static private final byte FRAME_ID_LONG = (byte) 0x68;
	static private final byte FRAME_END = (byte) 0x16;

	/** Control byte values */
	static protected final byte CTRL_REQUEST = 0x40; // request bit
	static protected final byte CTRL_FCB = 0x20; // frame count bit
	static protected final byte CTRL_FCV = 0x10; // frame count valid
	static protected final byte CTRL_RESET = 0x00;
	static protected final byte CTRL_USER = 0x03;
	static protected final byte CTRL_TICK = 0x04;
	static protected final byte CTRL_VEHICLE = 0x08;
	static protected final byte CTRL_STATUS = 0x09;
	static protected final byte CTRL_STATUS_RESP = 0x0B;

	/** Check if a drop address is valid */
	static private boolean isAddressValid(int drop) {
		return drop > 0 && drop <= 255;
	}

	/** Format a short frame request */
	protected final byte[] formatShort(byte ctrl, ControllerImpl c)
		throws IOException
	{
		int drop = c.getDrop();
		if (!isAddressValid(drop))
			throw new InvalidAddressException(drop);
		ctrl |= CTRL_REQUEST;
		byte adr = (byte) drop;
		int sum = (ctrl & 0xFF) + (adr & 0xFF);
		byte[] frame = new byte[5];
		frame[0] = FRAME_ID_SHORT;
		frame[1] = ctrl;
		frame[2] = adr;
		frame[3] = (byte) sum;
		frame[4] = FRAME_END;
		return frame;
	}

	/** Format a long frame request */
	protected final byte[] formatLong(byte ctrl, ControllerImpl c,
		byte[] data) throws IOException
	{
		if (8 + data.length >= 256)
			throw new ProtocolException("Data length");
		int drop = c.getDrop();
		if (!isAddressValid(drop))
			throw new InvalidAddressException(drop);
		ctrl |= CTRL_REQUEST;
		byte adr = (byte) drop;
		int len = data.length;
		byte[] frame = new byte[8 + len];
		frame[0] = FRAME_ID_LONG;
		frame[1] = (byte) (2 + len);
		frame[2] = frame[1];
		frame[3] = FRAME_ID_LONG;
		frame[4] = ctrl;
		frame[5] = adr;
		int sum = (ctrl & 0xFF) + (adr & 0xFF);
		for (int i = 0; i < len; i++) {
			frame[6 + i] = data[i];
			sum += data[i] & 0xFF;
		}
		frame[6 + len] = (byte) sum;
		frame[7 + len] = FRAME_END;
		return frame;
	}

	/** Parse a single byte frame response */
	protected final void parseSingle(InputStream is) throws IOException {
		byte[] frame = recvResponse(is, 1);
		if (frame[0] != FRAME_ID_SINGLE) {
			throw new ParsingException("Unexpected frame: " +
				frame[0]);
		}
	}

	/** Parse a long frame response.
	 * @return Application data, or null for single byte frame. */
	protected final byte[] parseLong(InputStream is, ControllerImpl c)
		throws IOException
	{
		int drop = c.getDrop();
		if (!isAddressValid(drop))
			throw new InvalidAddressException(drop);
		byte adr = (byte) drop;
		byte[] fid = recvResponse(is, 1);
		if (fid[0] == FRAME_ID_SINGLE)
			return null;
		else if (fid[0] != FRAME_ID_LONG)
			throw new ParsingException("Wrong frame ID: "+ fid[0]);
		byte[] head = recvResponse(is, 3);
		if (head[0] != head[1])
			throw new ParsingException("Frame length mismatch");
		if (head[2] != FRAME_ID_LONG)
			throw new ParsingException("Wrong frame ID: "+head[2]);
		int len = head[0] & 0xFF;
		if (len < 3)
			throw new ParsingException("Invalid length: " + len);
		byte[] tail = recvResponse(is, len + 2);
		if (tail[len + 1] != FRAME_END)
			throw new ParsingException("Wrong END: "+tail[len + 1]);
		int sum = 0;
		for (int i = 0; i < len; i++)
			sum += tail[i] & 0xFF;
		if (tail[len] != (byte) sum)
			throw new ChecksumException(tail);
		if (tail[1] != adr)
			throw new ParsingException("Wrong ADR: " + tail[1]);
		byte[] buf = new byte[len];
		System.arraycopy(tail, 0, buf, 0, len);
		return buf;
	}
}
