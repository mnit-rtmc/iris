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
package us.mn.state.dot.tms.server.comm.manchester;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.InvalidAddressException;

/**
 * A manchester property.
 *
 * @author Douglas Lau
 */
abstract public class ManchesterProperty extends ControllerProperty {

	/** Pan/tilt command bit masks (second byte) */
	static protected final byte PT_TILT_DOWN = 0x00;	// xx00 xxxx
	static protected final byte PT_TILT_UP = 0x10;		// xx01 xxxx
	static protected final byte PT_PAN_LEFT = 0x20;		// xx10 xxxx
	static protected final byte PT_PAN_RIGHT = 0x30;	// xx11 xxxx

	/** Extended command bit masks (second byte) */
	static protected final byte EX_TILT_DOWN_FULL = 0x00;	// xx00 000x
	static protected final byte EX_IRIS_OPEN = 0x02;	// xx00 001x
	static protected final byte EX_FOCUS_FAR = 0x04;	// xx00 010x
	static protected final byte EX_ZOOM_IN = 0x06;		// xx00 011x
	static protected final byte EX_IRIS_CLOSE = 0x08;	// xx00 100x
	static protected final byte EX_FOCUS_NEAR = 0x0A;	// xx00 101x
	static protected final byte EX_ZOOM_OUT = 0x0C;		// xx00 110x
	static protected final byte EX_PAN_LEFT_FULL = 0x0E;	// xx00 111x
	static protected final byte EX_TILT_UP_FULL = 0x10;	// xx01 000x
	static protected final byte EX_PAN_RIGHT_FULL = 0x12;	// xx01 001x
	static protected final byte EX_AUX_1 = 0x14;		// xx01 010x
	static protected final byte EX_AUX_4 = 0x16;		// xx01 011x
	static protected final byte EX_AUX_2 = 0x18;		// xx01 100x
	static protected final byte EX_AUX_5 = 0x1A;		// xx01 101x
	static protected final byte EX_AUX_3 = 0x1C;		// xx01 110x
	static protected final byte EX_AUX_6 = 0x1E;		// xx01 111x
	static protected final byte EX_RECALL_PRESET = 0x20;	// xx10 xxxx
	static protected final byte EX_STORE_PRESET = 0x30;	// xx11 xxxx

	/** Encode a speed value for pan/tilt command */
	static protected byte encodeSpeed(int v) {
		return (byte)(((Math.abs(v) - 1) << 1) & 0x0E);
	}

	/** Highest allowed address for Manchester protocol */
	static private final int ADDRESS_MAX = 1024;

	/** Check if a drop address is valid */
	static private boolean isAddressValid(int drop) {
		return drop >= 1 && drop <= ADDRESS_MAX;
	}

	/** Create manchester packet */
	private byte[] createPacket(int drop) throws IOException {
		if (!isAddressValid(drop))
			throw new InvalidAddressException(drop);
		byte[] pkt = new byte[3];
		pkt[0] = (byte)(0x80 | (drop >> 6));
		pkt[1] = (byte)((drop >> 5) & 0x01);
		pkt[2] = (byte)((drop & 0x1f) << 2);
		pkt[1] |= commandBits();
		if (!isExtended())
			pkt[2] |= 0x02;
		return pkt;
	}

	/** Get command bits */
	abstract protected byte commandBits();

	/** Check if packet is extended function */
	protected boolean isExtended() {
		return false;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		// receiver address is zero-relative
		os.write(createPacket(c.getDrop() - 1));
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is) {
		// do not expect any response
	}
}
