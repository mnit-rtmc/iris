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

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.VideoMonitorHelper;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Monitor status property.  The CM9760-KBD sends this request to the matrix
 * a few times per second.  The matrix should send a monitor status response
 * back.  If logon is required, an empty monitor status response is sent.
 *
 * @author Douglas Lau
 */
public class MonStatusProp extends PelcoPProp {

	/** Monitor status request code */
	static public final int REQ_CODE = 0xBA;

	/** Monitor status response code */
	static public final int RESP_CODE = 0xB1;

	/** Get camera ID */
	static protected String getCamId(VideoMonitor vm) {
		Camera c = vm.getCamera();
		return (c != null)
		      ? c.getName()
		      : SystemAttrEnum.CAMERA_ID_BLANK.getString();
	}

	/** Parse a device UID */
	static protected Integer parseUID(String uid) {
		return CameraHelper.parseUID(uid);
	}

	/** Find a camera by UID */
	static protected Camera findCam(int cam) {
		// First, lookup a guessed name for camera
		Camera c = CameraHelper.lookup(buildCamName(cam));
		if (c != null)
			return c;
		else {
			// Guess not correct, do linear search
			return CameraHelper.findUID(cam);
		}
	}

	/** Build a camera name guess */
	static private String buildCamName(int cam) {
		StringBuilder sb = new StringBuilder();
		sb.append('C');
		sb.append(cam);
		while (sb.length() < 4)
			sb.insert(1, '0');
		return sb.toString();
	}

	/** Flag for monitor online status */
	static private final int BIT_ONLINE = 0x40;

	/** Flag for monitor locked status */
	static private final int BIT_LOCKED = 0x10;

	/** Logged in flag */
	private final boolean logged_in;

	/** Video monitor number */
	private int mon_num;

	/** Create a new monitor status property */
	public MonStatusProp(boolean l, int mn) {
		logged_in = l;
		mon_num = mn;
	}

	/** Decode a QUERY request from keyboard */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws ParsingException
	{
		int mlo = parseBCD2(rx_buf);
		if (parse8(rx_buf) != 1)
			throw new ParsingException("MON EXT");
		int mhi = parseBCD2(rx_buf);
		setMonNumber((100 * mhi) + mlo);
	}

	/** Encode a QUERY response to keyboard */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		format8(tx_buf, RESP_CODE);
		int mon = getMonNumber();
		if (logged_in && mon > 0) {
			int cam = getCamNumber();
			int chi = cam / 100;
			int clo = cam % 100;
			int mhi = mon / 100;
			int mlo = mon % 100;
			formatBCD2(tx_buf, mlo);
			format8(tx_buf, BIT_ONLINE);
			format8(tx_buf, 0);
			formatBCD2(tx_buf, chi);
			formatBCD2(tx_buf, clo);
			format32(tx_buf, 0);
			format8(tx_buf, 0);
			formatBCD2(tx_buf, chi);
			formatBCD2(tx_buf, clo);
			format16(tx_buf, 0);
			format8(tx_buf, 0);
			formatBCD2(tx_buf, mhi);
		} else {
			format8(tx_buf, 0);
			format8(tx_buf, 0);
		}
	}

	/** Get current camera ID on the selected video monitor */
	protected int getCamNumber() {
		VideoMonitor vm = VideoMonitorHelper.findUID(mon_num);
		if (vm != null) {
			Integer uid = parseUID(getCamId(vm));
			if (uid != null)
				return uid;
		}
		return 0;
	}

	/** Set the video monitor number */
	protected void setMonNumber(int m) {
		mon_num = m;
	}

	/** Get the video monitor number */
	protected int getMonNumber() {
		return mon_num;
	}
}
