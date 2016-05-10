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
import java.util.Arrays;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * This class creates a Cohu PTZ request to change auto-focus mode.
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class SetAFModeProperty extends CohuPTZProperty {

	/** Device request */
	private final DeviceRequest devReq;

	/** Create the property. */
	public SetAFModeProperty(DeviceRequest dr) {
		devReq = dr;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] pkt = new byte[6];		// max. msg size of 6
		int i = 0;
		pkt[i++] = (byte) 0xf8;
		pkt[i++] = (byte) c.getDrop();

		switch (devReq) {
		case CAMERA_FOCUS_MANUAL:
			pkt[i++] = (byte) 0x63;
			pkt[i++] = (byte) 0x46;
			pkt[i++] = (byte) 0x4d;
			break;
		case CAMERA_FOCUS_AUTO:
			pkt[i++] = (byte) 0x63;
			pkt[i++] = (byte) 0x46;
			pkt[i++] = (byte) 0x41;
			break;
		default:
			// Invalid device request
			return;
		}
		pkt[i] = calculateChecksum(pkt, 1, i - 1);
		os.write(Arrays.copyOf(pkt, i + 1));
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "auto-focus mode: " + devReq;
	}
}
