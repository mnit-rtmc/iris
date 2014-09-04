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
package us.mn.state.dot.tms.server.comm.viconptz;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A property to command a camera
 *
 * @author Douglas Lau
 */
public class CommandProperty extends ViconPTZProperty {

	/** Bit flag to command a pan right */
	static private final byte PAN_RIGHT = 1 << 5;

	/** Bit flag to command a pan left */
	static private final byte PAN_LEFT = 1 << 6;

	/** Bit flag to command a tilt up */
	static private final byte TILT_UP = 1 << 4;

	/** Bit flag to command a tilt down */
	static private final byte TILT_DOWN = 1 << 3;

	/** Bit flag to command a zoom in */
	static private final byte ZOOM_IN = 1 << 5;

	/** Bit flag to command a zoom out */
	static private final byte ZOOM_OUT = 1 << 6;

	/** Pan value (-1023 to 1023) */
	private final int pan;

	/** Tilt value (-1023 to 1023) */
	private final int tilt;

	/** Requested zoom value [-1, 1] :: [out, in] */
	private final int zoom;

	/** Create a new command property */
	public CommandProperty(int p, int t, int z) {
		pan = p;
		tilt = t;
		zoom = z;
	}

	/** Get bit flags to control panning */
	private byte getPanFlags() {
		if (pan < 0)
			return PAN_LEFT;
		else if (pan > 0)
			return PAN_RIGHT;
		else
			return 0;
	}

	/** Get bit flags to control tilting */
	private byte getTiltFlags() {
		if (tilt < 0)
			return TILT_DOWN;
		else if (tilt > 0)
			return TILT_UP;
		else
			return 0;
	}

	/** Get bit flags to control zooming */
	private byte getZoomFlags() {
		if (zoom < 0)
			return ZOOM_OUT;
		else if (zoom > 0)
			return ZOOM_IN;
		else
			return 0;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(OutputStream os, int drop) throws IOException {
		byte[] pkt = new byte[10];
		pkt[0] = (byte)(0x80 | (drop >> 4));
		pkt[1] = (byte)((0x0f & drop) | EXTENDED_CMD);
		pkt[2] = (byte)(getPanFlags() | getTiltFlags());
		pkt[3] = getZoomFlags();
		pkt[4] = (byte)0x00; // not implemented
		pkt[5] = (byte)0x00; // not implemented
		pkt[6] = (byte)((Math.abs(pan) >> 7) & 0x0f);
		pkt[7] = (byte)((byte)Math.abs(pan) & 0x7f);
		pkt[8] = (byte)((Math.abs(tilt) >> 7) & 0x0f);
		pkt[9] = (byte)((byte)Math.abs(tilt) & 0x7f);
		os.write(pkt);
	}
}
