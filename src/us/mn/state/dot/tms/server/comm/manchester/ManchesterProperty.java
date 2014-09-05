/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * A manchester property.
 *
 * @author Douglas Lau
 */
abstract public class ManchesterProperty extends ControllerProperty {

	/** Create manchester packet */
	protected byte[] createPacket(int drop) {
		byte[] pkt = new byte[3];
		pkt[0] = (byte)(0x80 | (drop >> 6));
		pkt[1] = (byte)((drop >> 5) & 0x01);
		pkt[2] = (byte)((drop & 0x1f) << 2);
		return pkt;
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(InputStream is, int drop) {
		// do not expect any response
	}
}
