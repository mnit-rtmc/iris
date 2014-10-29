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
package us.mn.state.dot.tms.server.comm.cohuptz;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * This class creates a Cohu PTZ request to change auto-focus mode.
 *
 * @author Travis Swanston
 */
public class SetAFModeProperty extends CohuPTZProperty {

	protected final DeviceRequest devReq;

	/** Create the property. */
	public SetAFModeProperty(DeviceRequest dr) {
		devReq = dr;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] message = new byte[6];		// max. msg size of 6
		int i = 0;
		message[i++] = (byte)0xf8;
		message[i++] = (byte)c.getDrop();

		boolean validRequest = true;
		switch (devReq) {
			case CAMERA_FOCUS_MANUAL:
				message[i++] = (byte)0x63;
				message[i++] = (byte)0x46;
				message[i++] = (byte)0x4d;
				break;
			case CAMERA_FOCUS_AUTO:
				message[i++] = (byte)0x63;
				message[i++] = (byte)0x46;
				message[i++] = (byte)0x41;
				break;
			default:
				validRequest = false;
				break;
		}
		if (validRequest) {
			message[i] = calculateChecksum(message, 1, i - 1);
			os.write(Arrays.copyOf(message, i + 1));
		}
	}
}
