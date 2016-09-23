/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.viconptz;

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Extended Vicon property.
 *
 * @author Douglas Lau
 */
abstract public class ExtendedProp extends ViconPTZProp {

	/** Mask for extended command requests (second byte) */
	static private final byte EXTENDED_CMD = 0x50;

	/** Get command parameter 1 */
	abstract protected int getParam1();

	/** Get command parameter 2 */
	abstract protected int getParam2();

	/** Encode a STORE request */
	@Override
	public void encodeStore(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		int d = getDrop(op);
		int p1 = getParam1();
		int p2 = getParam2();
		// Extended packets contain 10 bytes
		tx_buf.put((byte) (0x80 | (d >> 4)));
		tx_buf.put((byte) ((0x0f & d) | EXTENDED_CMD));
		tx_buf.put(panTiltFlags());
		tx_buf.put(lensFlags());
		tx_buf.put(auxBits());
		tx_buf.put(presetBits());
		tx_buf.put((byte) ((p1 >>> 7) & 0x7f));
		tx_buf.put((byte) ((p1 >>> 0) & 0x7f));
		tx_buf.put((byte) ((p2 >>> 7) & 0x7f));
		tx_buf.put((byte) ((p2 >>> 0) & 0x7f));
	}
}
