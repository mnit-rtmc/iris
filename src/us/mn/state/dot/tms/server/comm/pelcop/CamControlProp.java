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
package us.mn.state.dot.tms.server.comm.pelcop;

import java.nio.ByteBuffer;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import static us.mn.state.dot.tms.DeviceRequest.*;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Camera control property.
 *
 * @author Douglas Lau
 */
public class CamControlProp extends MonStatusProp {

	/** Camera control request code */
	static public final int REQ_CODE = 0xC0;

	/** Focus far bit flag */
	static private final int BIT_FOCUS_FAR = 1 << 0;

	/** Focus near bit flag */
	static private final int BIT_FOCUS_NEAR = 1 << 1;

	/** Iris open bit flag */
	static private final int BIT_IRIS_OPEN = 1 << 2;

	/** Iris close bit flag */
	static private final int BIT_IRIS_CLOSE = 1 << 3;

	/** Create a new camera control property */
	public CamControlProp(boolean l) {
		super(l);
	}

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		int mlo = parseBCD2(rx_buf);
		int cam = parseBCD4(rx_buf);
		int c0 = parse8(rx_buf);
		int c1 = parse8(rx_buf);
		int c2 = parse8(rx_buf);
		int c3 = parse8(rx_buf);
		int mhi = parseBCD2(rx_buf);
		if (parse8(rx_buf) != 0)
			throw new ParsingException("PTZ");
		monitor = (100 * mhi) + mlo;
		sendControl(cam, c0, c1, c2, c3);
	}

	/** Send control to camera */
	private void sendControl(int cam, int c0, int c1, int c2, int c3)
		throws ParsingException
	{
		// FIXME: this is another linear search
		Camera c = CameraHelper.findUID(cam);
		if (c != null) {
			if (c0 != 0) {
				sendFocusControl(c, c0);
				sendIrisControl(c, c0);
			}
			if ((c1 & 1) == 0)
				sendPTZ(c, c1, c2, c3);
		}
	}

	/** Send focus control */
	private void sendFocusControl(Camera c, int c0) throws ParsingException{
		int focus = c0 & (BIT_FOCUS_FAR | BIT_FOCUS_NEAR);
		switch (focus) {
		case BIT_FOCUS_FAR:
			c.setDeviceRequest(CAMERA_FOCUS_FAR.ordinal());
			break;
		case BIT_FOCUS_NEAR:
			c.setDeviceRequest(CAMERA_FOCUS_NEAR.ordinal());
			break;
		case 0:
			break;
		default:
			throw new ParsingException("FOCUS");
		}
	}

	/** Send iris control */
	private void sendIrisControl(Camera c, int c0) throws ParsingException {
		int iris = c0 & (BIT_IRIS_OPEN | BIT_IRIS_CLOSE);
		switch (iris) {
		case BIT_IRIS_OPEN:
			c.setDeviceRequest(CAMERA_IRIS_OPEN.ordinal());
			break;
		case BIT_IRIS_CLOSE:
			c.setDeviceRequest(CAMERA_IRIS_CLOSE.ordinal());
			break;
		case 0:
			break;
		default:
			throw new ParsingException("IRIS");
		}
	}

	/** Send PTZ commands */
	private void sendPTZ(Camera c, int c1, int c2, int c3)
		throws ParsingException
	{
		// FIXME
	}
}
