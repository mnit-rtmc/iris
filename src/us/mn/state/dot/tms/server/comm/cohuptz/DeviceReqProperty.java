/*
 * IRIS -- Intelligent Roadway Information System
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
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Cohu device request property.
 *
 * @author Douglas Lau
 */
public class DeviceReqProperty extends CohuPTZProperty {

	/** Reset camera command */
	static private final byte[] CMD_RESET_CAMERA = new byte[] {
		(byte) 0x72, (byte) 0x73
	};

	/** Focus stop command */
	static private final byte[] CMD_FOCUS_STOP = new byte[] {
		(byte) 0x46, (byte) 0x53
	};

	/** Focus near command */
	static private final byte[] CMD_FOCUS_NEAR = new byte[] {
		(byte) 0x46, (byte) 0x4e
	};

	/** Focus far command */
	static private final byte[] CMD_FOCUS_FAR = new byte[] {
		(byte) 0x46, (byte) 0x46
	};

	/** Manual focus command */
	static private final byte[] CMD_FOCUS_MANUAL = new byte[] {
		(byte) 0x63, (byte) 0x46, (byte) 0x4d
	};

	/** Auto focus command */
	static private final byte[] CMD_FOCUS_AUTO = new byte[] {
		(byte) 0x63, (byte) 0x46, (byte) 0x41
	};

	/** Iris stop command */
	static private final byte[] CMD_IRIS_STOP = new byte[] {
		(byte) 0x49, (byte) 0x53
	};

	/** Iris close command */
	static private final byte[] CMD_IRIS_CLOSE = new byte[] {
		(byte) 0x49, (byte) 0x43
	};

	/** Iris open command */
	static private final byte[] CMD_IRIS_OPEN = new byte[] {
		(byte) 0x49, (byte) 0x4f
	};

	/** Manual iris command */
	static private final byte[] CMD_IRIS_MANUAL = new byte[] {
		(byte) 0x63, (byte) 0x49, (byte) 0x4d
	};

	/** Auto iris command */
	static private final byte[] CMD_IRIS_AUTO = new byte[] {
		(byte) 0x63, (byte) 0x49, (byte) 0x41
	};

	/** Device request */
	private final DeviceRequest req;

	/** Create the property */
	public DeviceReqProperty(DeviceRequest dr) {
		req = dr;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		os.write(createPacket(c.getDrop(), createCmd()));
	}

	/** Create the device request command */
	private byte[] createCmd() throws ProtocolException {
		switch (req) {
		case RESET_DEVICE:
			return CMD_RESET_CAMERA;
		case CAMERA_IRIS_STOP:
			return CMD_IRIS_STOP;
		case CAMERA_IRIS_CLOSE:
			return CMD_IRIS_CLOSE;
		case CAMERA_IRIS_OPEN:
			return CMD_IRIS_OPEN;
		case CAMERA_IRIS_MANUAL:
			return CMD_IRIS_MANUAL;
		case CAMERA_IRIS_AUTO:
			return CMD_IRIS_AUTO;
		case CAMERA_FOCUS_STOP:
			return CMD_FOCUS_STOP;
		case CAMERA_FOCUS_NEAR:
			return CMD_FOCUS_NEAR;
		case CAMERA_FOCUS_FAR:
			return CMD_FOCUS_FAR;
		case CAMERA_FOCUS_MANUAL:
			return CMD_FOCUS_MANUAL;
		case CAMERA_FOCUS_AUTO:
			return CMD_FOCUS_AUTO;
		default:
			throw new ProtocolException("INVALID DEVICE REQ");
		}
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "device req: " + req;
	}
}
