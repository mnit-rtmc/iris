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
 * A property to command a camera.
 *
 * @author Douglas Lau
 */
public class CommandProperty extends ManchesterProperty {

	/** Pan/tilt command bit masks (second byte) */
	static private final int PT_TILT_DOWN = 0x00;		// xx00 xxxx
	static private final int PT_TILT_UP = 0x10;		// xx01 xxxx
	static private final int PT_PAN_LEFT = 0x20;		// xx10 xxxx
	static private final int PT_PAN_RIGHT = 0x30;		// xx11 xxxx

	/** Extended command bit masks (second byte) */
	static private final int EX_TILT_DOWN_FULL = 0x00;	// xx00 000x
	static private final int EX_IRIS_OPEN = 0x02;		// xx00 001x
	static private final int EX_FOCUS_FAR = 0x04;		// xx00 010x
	static private final int EX_ZOOM_IN = 0x06;		// xx00 011x
	static private final int EX_IRIS_CLOSE = 0x08;		// xx00 100x
	static private final int EX_FOCUS_NEAR = 0x0A;		// xx00 101x
	static private final int EX_ZOOM_OUT = 0x0C;		// xx00 110x
	static private final int EX_PAN_LEFT_FULL = 0x0E;	// xx00 111x
	static private final int EX_TILT_UP_FULL = 0x10;	// xx01 000x
	static private final int EX_PAN_RIGHT_FULL = 0x12;	// xx01 001x
	static private final int EX_AUX_1 = 0x14;		// xx01 010x
	static private final int EX_AUX_4 = 0x16;		// xx01 011x
	static private final int EX_AUX_2 = 0x18;		// xx01 100x
	static private final int EX_AUX_5 = 0x1A;		// xx01 101x
	static private final int EX_AUX_3 = 0x1C;		// xx01 110x
	static private final int EX_AUX_6 = 0x1E;		// xx01 111x

	/** Encode a speed value for pan/tilt command */
	static private byte encodeSpeed(int v) {
		return (byte)(((Math.abs(v) - 1) << 1) & 0x0E);
	}

	/** Pan value (-7 to 7) (8 means turbo) */
	private final int pan;

	/** Tilt value (-7 to 7) (8 means turbo) */
	private final int tilt;

	/** Zoom value (-1 to 1) */
	private final int zoom;

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
				pkt[1] |= PT_PAN_LEFT;
			else
				pkt[1] |= PT_PAN_RIGHT;
			pkt[1] |= encodeSpeed(pan);
			pkt[2] |= 0x02;
		} else {
			if (pan < 0)
				pkt[1] |= EX_PAN_LEFT_FULL;
			else
				pkt[1] |= EX_PAN_RIGHT_FULL;
		}
		return pkt;
	}

	/** Encode a tilt command packet */
	private byte[] encodeTiltPacket(int drop) {
		byte[] pkt = createPacket(drop);
		if (Math.abs(tilt) < 8) {
			if (tilt <= 0)
				pkt[1] |= PT_TILT_DOWN;
			else
				pkt[1] |= PT_TILT_UP;
			pkt[1] |= encodeSpeed(tilt);
			pkt[2] |= 0x02;
		} else {
			if (tilt < 0)
				pkt[1] |= EX_TILT_DOWN_FULL;
			else
				pkt[1] |= EX_TILT_UP_FULL;
		}
		return pkt;
	}

	/** Encode a zoom command packet */
	private byte[] encodeZoomPacket(int drop) {
		byte[] pkt = createPacket(drop);
		if (zoom < 0)
			pkt[1] |= EX_ZOOM_OUT;
		else
			pkt[1] |= EX_ZOOM_IN;
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
