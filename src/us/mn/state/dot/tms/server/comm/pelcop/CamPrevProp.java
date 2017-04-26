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
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.VideoMonitorImpl;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Camera previous select property.  This message selects the previous (lower-
 * numbered) camera on a monitor.
 *
 * @author Douglas Lau
 */
public class CamPrevProp extends MonStatusProp {

	/** Camera previous request code */
	static public final int REQ_CODE = 0xB7;

	/** Find previous camera */
	static private CameraImpl findPrev(int uid) {
		Camera c = CameraHelper.findPrev(uid);
		if (c instanceof CameraImpl)
			return (CameraImpl) c;
		c = CameraHelper.findLast();
		if (c instanceof CameraImpl)
			return (CameraImpl) c;
		else
			return null;
	}

	/** Create a new camera previous property */
	public CamPrevProp(boolean l, int mn) {
		super(l, mn);
	}

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		int mlo = parseBCD2(rx_buf);
		int mhi = parseBCD2(rx_buf);
		setMonNumber((100 * mhi) + mlo);
		selectPrevCamera(op);
	}

	/** Select previous camera on a video monitor */
	private void selectPrevCamera(Operation op) {
		int uid = getCamNumber();
		if (uid > 0) {
			CameraImpl c = findPrev(uid);
			if (c != null) {
				VideoMonitorImpl.setCameraNotify(getMonNumber(),
					c, "PREV " + op.getId());
			}
		}
	}
}
