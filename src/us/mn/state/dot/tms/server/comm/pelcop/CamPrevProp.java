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
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.VideoMonitorHelper;
import us.mn.state.dot.tms.server.VideoMonitorImpl;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Camera previous select property.
 *
 * @author Douglas Lau
 */
public class CamPrevProp extends MonStatusProp {

	/** Camera previous request code */
	static public final int REQ_CODE = 0xB7;

	/** Create a new camera previous property */
	public CamPrevProp(boolean l) {
		super(l);
	}

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		int mlo = parseBCD2(rx_buf);
		int mhi = parseBCD2(rx_buf);
		monitor = (100 * mhi) + mlo;
		selectPrevCamera();
	}

	/** Select previous camera on a video monitor */
	private void selectPrevCamera() {
		// FIXME: this is a linear search
		VideoMonitor vm = VideoMonitorHelper.findUID(monitor);
		if (vm instanceof VideoMonitorImpl) {
			VideoMonitorImpl mon = (VideoMonitorImpl) vm;
			Integer uid = CameraHelper.parseUID(getCamId(vm));
			if (uid != null) {
				// FIXME: this is another linear search
				Camera c = CameraHelper.findPrev(uid);
				if (c != null)
					mon.setCameraNotify(c);
			}
		}
	}
}
