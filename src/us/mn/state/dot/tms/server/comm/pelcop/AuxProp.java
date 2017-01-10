/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.CameraImpl;
import static us.mn.state.dot.tms.DeviceRequest.CAMERA_WIPER_ONESHOT;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Auxilliary property.  This message sets or clears a camera aux function.
 *
 * @author Douglas Lau
 */
public class AuxProp extends MonStatusProp {

	/** Aux request code */
	static public final int REQ_CODE = 0xB8;

	/** Aux bit for ON */
	static private final int AUX_BIT_ON = 0x01;

	/** Aux bit mask */
	static private final int AUX_MASK = 0x0E;

	/** Aux value for wiper */
	static private final int AUX_WIPER = 0x04;

	/** Aux value for Cam ON */
	static private final int AUX_CAM_ON = 0x08;

	/** Aux value for Cam OFF */
	static private final int AUX_CAM_OFF = 0x0E;

	/** Create a new aux property */
	public AuxProp(boolean l, int mn) {
		super(l, mn);
	}

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		int cam = parseBCD4(rx_buf);
		int aux = parse8(rx_buf);
		int mlo = parseBCD2(rx_buf);
		int mhi = parseBCD2(rx_buf);
		if (parse8(rx_buf) != 0)
			throw new ParsingException("AUX");
		setMonNumber((100 * mhi) + mlo);
		sendAux(cam, aux);
	}

	/** Send aux command to camera */
	private void sendAux(int cam, int aux) throws ParsingException {
		CameraImpl c = findCamera(cam);
		if (c != null) {
			if ((aux & AUX_BIT_ON) == 0)
				return;
			switch (aux & AUX_MASK) {
			case AUX_WIPER:
				c.setDeviceRequest(
					CAMERA_WIPER_ONESHOT.ordinal());
				return;
			case AUX_CAM_ON:
			case AUX_CAM_OFF:
				// FIXME
				return;
			default:
				throw new ParsingException("AUX");
			}
		}
		throw new ParsingException("AUX CAM");
	}
}
