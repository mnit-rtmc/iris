/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.pelcod;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Pelco D command property for initiating a camera reset.
 *
 * @author Travis Swanston
 */
public class ResetCameraProperty extends PelcoDProperty {

	/** Create the property. */
	public ResetCameraProperty() {
	}

	/** Encode a STORE request. */
	public void encodeStore(OutputStream os, int drop) throws IOException {
		byte[] message = new byte[7];
		message[0] = (byte)0xff;
		message[1] = (byte)drop;
		message[2] = (byte)0x00;
		message[3] = (byte)0x0f;
		message[4] = (byte)0x00;
		message[5] = (byte)0x00;
		message[6] = calculateChecksum(message);
		os.write(message);
	}

}
