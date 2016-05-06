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
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * This class creates a Cohu PTZ request to initiate an iris movement.
 *
 * @author Travis Swanston
 */
public class MoveIrisProperty extends CohuPTZProperty {

	protected final DeviceRequest devReq;

	/** Create the property. */
	public MoveIrisProperty(DeviceRequest dr) {
		devReq = dr;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] message = createPacket(c.getDrop());
		int i = 0;
		message[i++] = (byte)0xf8;
		message[i++] = (byte)c.getDrop();

		boolean validRequest = true;
		switch (devReq) {
			case CAMERA_IRIS_STOP:
				message[i++] = (byte)0x49;
				message[i++] = (byte)0x53;
				break;
			case CAMERA_IRIS_CLOSE:
				message[i++] = (byte)0x49;
				message[i++] = (byte)0x43;
				break;
			case CAMERA_IRIS_OPEN:
				message[i++] = (byte)0x49;
				message[i++] = (byte)0x4f;
				break;
			default:
				validRequest = false;
				break;
		}
		if (validRequest) {
			message[i] = calculateChecksum(message, 1, i - 1);
			os.write(message);
		}
	}
}
