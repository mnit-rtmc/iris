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
package us.mn.state.dot.tms.server.comm.viconptz;

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.ControllerProp;
import us.mn.state.dot.tms.server.comm.InvalidAddressException;

/**
 * Vicon Property
 *
 * @author Douglas Lau
 */
abstract public class ViconPTZProp extends ControllerProp {

	/** Mask for command requests (second byte) */
	static private final byte CMD = 0x10;

	/** Highest allowed address for Vicon protocol */
	static private final int ADDRESS_MAX = 254;

	/** Check if a drop address is valid */
	static private boolean isAddressValid(int drop) {
		return drop >= 1 && drop <= ADDRESS_MAX;
	}

	/** Drop address */
	protected final int drop;

	/** Create a new Vicon PTZ property */
	protected ViconPTZProp(int d) throws IOException {
		if (!isAddressValid(d))
			throw new InvalidAddressException(d);
		drop = d;
	}

	/** Get the pan/tilt flags */
	protected byte panTiltFlags() {
		return 0;
	}

	/** Get the lens flags */
	protected byte lensFlags() {
		return 0;
	}

	/** Get the aux bits */
	protected byte auxBits() {
		return 0;
	}

	/** Get the preset bits */
	protected byte presetBits() {
		return 0;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ByteBuffer tx_buf) {
		// Command packets contain 6 bytes
		tx_buf.put((byte) (0x80 | (drop >> 4)));
		tx_buf.put((byte) ((0x0f & drop) | CMD));
		tx_buf.put(panTiltFlags());
		tx_buf.put(lensFlags());
		tx_buf.put(auxBits());
		tx_buf.put(presetBits());
	}
}
