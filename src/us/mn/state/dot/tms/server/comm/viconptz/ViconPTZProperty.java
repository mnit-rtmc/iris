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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.InvalidAddressException;

/**
 * Vicon Property
 *
 * @author Douglas Lau
 */
abstract public class ViconPTZProperty extends ControllerProperty {

	/** Mask for command requests (second byte) */
	static private final byte CMD = 0x10;

	/** Highest allowed address for Vicon protocol */
	static private final int ADDRESS_MAX = 254;

	/** Check if a drop address is valid */
	static private boolean isAddressValid(int drop) {
		return drop >= 1 && drop <= ADDRESS_MAX;
	}

	/** Create basic Vicon packet */
	protected byte[] createPacket(int drop) throws IOException {
		if (!isAddressValid(drop))
			throw new InvalidAddressException(drop);
		byte[] pkt = new byte[6];
		pkt[0] = (byte)(0x80 | (drop >> 4));
		pkt[1] = (byte)((0x0f & drop) | CMD);
		pkt[2] = panTiltFlags();
		pkt[3] = lensFlags();
		pkt[4] = auxBits();
		pkt[5] = presetBits();
		return pkt;
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
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		os.write(createPacket(c.getDrop()));
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is) {
		// do not expect any response
	}
}
