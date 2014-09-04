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

	/** P/T flag to command a tilt down */
	static private final byte TILT_DOWN = 1 << 3;

	/** P/T flag to command a tilt up */
	static private final byte TILT_UP = 1 << 4;

	/** P/T flag to command a pan right */
	static private final byte PAN_RIGHT = 1 << 5;

	/** P/T flag to command a pan left */
	static private final byte PAN_LEFT = 1 << 6;

	/** Lens flag to command iris close */
	static private final byte IRIS_CLOSE = 1 << 1;

	/** Lens flag to command iris open */
	static private final byte IRIS_OPEN = 1 << 2;

	/** Lens flag to command focus near */
	static private final byte FOCUS_NEAR = 1 << 3;

	/** Lens flag to command focus far */
	static private final byte FOCUS_FAR = 1 << 4;

	/** Lens flag to command a zoom in */
	static private final byte ZOOM_IN = 1 << 5;

	/** Lens flag to command a zoom out */
	static private final byte ZOOM_OUT = 1 << 6;

	/** Pan value (-1023 to 1023) */
	private final int pan;

	/** Tilt value (-1023 to 1023) */
	private final int tilt;

	/** Requested zoom value [-1, 1] :: [out, in] */
	private final int zoom;

	/** Requested focus value [-1, 1] :: [near, far] */
	private final int focus;

	/** Requested iris value [-1, 1] :: [close, open] */
	private final int iris;

	/** Create a new command property */
	public CommandProperty(int p, int t, int z, int f, int i) {
		pan = p;
		tilt = t;
		zoom = z;
		focus = f;
		iris = i;
	}

	/** Get pan/tilt flags */
	private byte panTiltFlags() {
		return (byte)(panFlags() | tiltFlags());
	}

	/** Get bit flags to control panning */
	private byte panFlags() {
		if (pan < 0)
			return PAN_LEFT;
		else if (pan > 0)
			return PAN_RIGHT;
		else
			return 0;
	}

	/** Get bit flags to control tilting */
	private byte tiltFlags() {
		if (tilt < 0)
			return TILT_DOWN;
		else if (tilt > 0)
			return TILT_UP;
		else
			return 0;
	}

	/** Get bit flags to control lens */
	private byte lensFlags() {
		return (byte)(irisFlags() | focusFlags() | zoomFlags());
	}

	/** Get lens flags to control iris */
	private byte irisFlags() {
		if (iris < 0)
			return IRIS_CLOSE;
		else if (iris > 0)
			return IRIS_OPEN;
		else
			return 0;
	}

	/** Get lens flags to control focus */
	private byte focusFlags() {
		if (focus < 0)
			return FOCUS_NEAR;
		else if (focus > 0)
			return FOCUS_FAR;
		else
			return 0;
	}

	/** Get lens flags to control zoom */
	private byte zoomFlags() {
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
		pkt[2] = panTiltFlags();
		pkt[3] = lensFlags();
		pkt[4] = (byte)0x00; // not implemented
		pkt[5] = (byte)0x00; // not implemented
		pkt[6] = (byte)((Math.abs(pan) >> 7) & 0x0f);
		pkt[7] = (byte)((byte)Math.abs(pan) & 0x7f);
		pkt[8] = (byte)((Math.abs(tilt) >> 7) & 0x0f);
		pkt[9] = (byte)((byte)Math.abs(tilt) & 0x7f);
		os.write(pkt);
	}
}
