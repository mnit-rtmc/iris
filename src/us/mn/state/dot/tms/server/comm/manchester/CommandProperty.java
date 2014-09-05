/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.manchester;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A property to command a camera
 *
 * @author Douglas Lau
 */
public class CommandProperty extends ManchesterProperty {

	static private final int EX_TILT_DOWN_FULL = 0;
	static private final int EX_IRIS_OPEN = 1;
	static private final int EX_FOCUS_FAR = 2;
	static private final int EX_ZOOM_IN = 3;
	static private final int EX_IRIS_CLOSE = 4;
	static private final int EX_FOCUS_NEAR = 5;
	static private final int EX_ZOOM_OUT = 6;
	static private final int EX_PAN_LEFT_FULL = 7;
	static private final int EX_TILT_UP_FULL = 8;
	static private final int EX_PAN_RIGHT_FULL = 9;

	/** Encode a speed value for pan/tilt command */
	static private byte encodeSpeed(int v) {
		return (byte)((Math.abs(v) - 1) << 1);
	}

	/** Pan value (-7 to 7) (8 means turbo) */
	protected final int pan;

	/** Tilt value (-7 to 7) (8 means turbo) */
	protected final int tilt;

	/** Zoom value (-1 to 1) */
	protected final int zoom;

	/** Create a new command property */
	public CommandProperty(int p, int t, int z) {
		pan = p;
		tilt = t;
		zoom = z;
	}

	/** Encode a pan command packet */
	private byte[] encodePanPacket(int drop) {
		byte[] pkt = createPacket(drop);
		if (Math.abs(pan) < 8) {
			if (pan < 0)
				pkt[1] |= 0x20;
			else
				pkt[1] |= 0x30;
			pkt[1] |= encodeSpeed(pan);
			pkt[2] |= 0x02;
		} else {
			if (pan < 0)
				pkt[1] |= EX_PAN_LEFT_FULL << 1;
			else
				pkt[1] |= EX_PAN_RIGHT_FULL << 1;
		}
		return pkt;
	}

	/** Encode a tilt command packet */
	private byte[] encodeTiltPacket(int drop) {
		byte[] pkt = createPacket(drop);
		if (Math.abs(tilt) < 8) {
			if (tilt > 0)
				pkt[1] |= 0x10;
			pkt[1] |= encodeSpeed(tilt);
			pkt[2] |= 0x02;
		} else {
			if (tilt < 0)
				pkt[1] |= EX_TILT_DOWN_FULL << 1;
			else
				pkt[1] |= EX_TILT_UP_FULL << 1;
		}
		return pkt;
	}

	/** Encode a zoom command packet */
	private byte[] encodeZoomPacket(int drop) {
		byte[] pkt = createPacket(drop);
		if (zoom < 0)
			pkt[1] |= EX_ZOOM_OUT << 1;
		else
			pkt[1] |= EX_ZOOM_IN << 1;
		return pkt;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(OutputStream os, int drop) throws IOException {
		drop--;		// receiver address is zero-relative
		if (pan != 0)
			os.write(encodePanPacket(drop));
		if (tilt != 0)
			os.write(encodeTiltPacket(drop));
		if (zoom != 0)
			os.write(encodeZoomPacket(drop));
	}
}
