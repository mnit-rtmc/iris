/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cohuptz;

import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * This class creates a Cohu PTZ request to instruct a camera
 * to store the current state to a specified preset.
 *
 * @author Travis Swanston
 */
public class StorePresetProperty extends CohuPTZProperty {

	/** Requested preset to store */
	private final int preset;

	/** Create a new store preset property */
	public StorePresetProperty(int p) {
		preset = p;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		Byte presetByte = getPresetByte(preset);
		if (presetByte == null)
			return;
		byte pb = presetByte.byteValue();

		byte[] message = createPacket(c.getDrop());
		message[0] = (byte)0xf8;
		message[1] = (byte)c.getDrop();
		message[2] = (byte)0x50;
		message[3] = pb;
		message[4] = calculateChecksum(message, 1, 3);
		os.write(message);
	}
}
