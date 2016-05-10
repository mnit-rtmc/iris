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
 * @author Douglas Lau
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
		Byte p = getPresetByte(preset);
		if (p != null) {
			byte[] cmd = new byte[2];
			cmd[0] = (byte) 0x50;
			cmd[1] = p;
			os.write(createPacket(c.getDrop(), cmd));
		}
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "store preset: " + preset;
	}
}
