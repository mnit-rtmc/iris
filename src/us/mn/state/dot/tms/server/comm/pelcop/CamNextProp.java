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
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.VideoMonitorImpl;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Camera next select property.  This message selects the next (higher-numbered)
 * camera on a monitor.
 *
 * @author Douglas Lau
 */
public class CamNextProp extends MonStatusProp {

	/** Camera next request code */
	static public final int REQ_CODE = 0xB6;

	/** Create a new camera next property */
	public CamNextProp(boolean l, VideoMonitorImpl vm) {
		super(l, vm);
	}

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		int mlo = parseBCD2(rx_buf);
		int mhi = parseBCD2(rx_buf);
		setMonNumber((100 * mhi) + mlo);
		selectNextCamera(op);
	}

	/** Select next camera on a video monitor */
	private void selectNextCamera(Operation op) {
		VideoMonitorImpl vm = getMonitor();
		if (vm != null) {
			Integer uid = parseUID(getCamId(vm));
			if (uid != null) {
				Camera c = CameraHelper.findNext(uid);
				if (c instanceof CameraImpl) {
					vm.setCameraNotify((CameraImpl) c,
					                   "NEXT " + op);
				}
			}
		}
	}
}
