/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.VideoMonitorImpl;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Camera select property.  This message selects a camera on a monitor.
 *
 * @author Douglas Lau
 */
public class CamSelectProp extends MonStatusProp {

	/** Camera select request code */
	static public final int REQ_CODE = 0xB2;

	/** Create a new camera select property */
	public CamSelectProp(boolean l, int mn) {
		super(l, mn);
	}

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		int mlo = parseBCD2(rx_buf);
		int cam = parseBCD4(rx_buf);
		int mhi = parseBCD2(rx_buf);
		if (parse8(rx_buf) != 0)
			throw new ParsingException("CAM EXT");
		setMonNumber((100 * mhi) + mlo);
		selectCamera(op, cam);
	}

	/** Select camera on a video monitor */
	private void selectCamera(Operation op, int cam) {
		Camera c = findCam(cam);
		if (c instanceof CameraImpl) {
			VideoMonitorImpl.setCameraNotify(getMonNumber(),
				(CameraImpl) c, "SELECT " + op);
		}
	}
}
