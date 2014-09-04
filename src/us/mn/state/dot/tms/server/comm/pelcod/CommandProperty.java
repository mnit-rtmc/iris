/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
 * Copyright (C) 2014  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.pelcod;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A property to command a camera
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class CommandProperty extends PelcoDProperty {

	/** Bit flag to command a pan right */
	static protected final byte PAN_RIGHT = 1 << 1;

	/** Bit flag to command a pan left */
	static protected final byte PAN_LEFT = 1 << 2;

	/** Bit flag to command a tilt up */
	static protected final byte TILT_UP = 1 << 3;

	/** Bit flag to command a tilt down */
	static protected final byte TILT_DOWN = 1 << 4;

	/** Bit flag to command a zoom in */
	static protected final byte ZOOM_IN = 1 << 5;

	/** Bit flag to command a zoom out */
	static protected final byte ZOOM_OUT = 1 << 6;

	/** Bit flag to command a focus-far op */
	static protected final int FOCUS_FAR = 1 << 7;

	/** Bit flag to command a focus-near op */
	static protected final int FOCUS_NEAR = 1 << 8;

	/** Bit flag to command an iris-open op */
	static protected final int IRIS_OPEN = 1 << 9;

	/** Bit flag to command an iris-close op */
	static protected final int IRIS_CLOSE = 1 << 10;

	/** Requested pan value [-63, 63] :: [left, right] (64 is turbo) */
	protected final int pan;

	/** Requested tilt value [-63, 63] :: [down, up] */
	protected final int tilt;

	/** Requested zoom value [-1, 1] :: [out, in] */
	protected final int zoom;

	/** Requested focus value [-1, 1] :: [near, far] */
	protected final int focus;

	/** Requested iris value [-1, 1] :: [close, open] */
	protected final int iris;

	/** Create a new command property */
	public CommandProperty(int p, int t, int z, int f, int i) {
		pan = p;
		tilt = t;
		zoom = z;
		focus = f;
		iris = i;
	}

	/** Construct an int containing the pan command flags in the 2 LSBs */
	protected byte getPanFlags() {
		if (pan < 0)
			return PAN_LEFT;
		else if (pan > 0)
			return PAN_RIGHT;
		else
			return 0;
	}

	/** Construct an int containing the tilt command flags in the 2 LSBs */
	protected byte getTiltFlags() {
		if (tilt < 0)
			return TILT_DOWN;
		else if (tilt > 0)
			return TILT_UP;
		else
			return 0;
	}

	/** Construct an int containing the zoom command flags in the 2 LSBs */
	protected byte getZoomFlags() {
		if (zoom < 0)
			return ZOOM_OUT;
		else if (zoom > 0)
			return ZOOM_IN;
		else
			return 0;
	}

	/** Construct an int containing the focus command flags in the 2 LSBs */
	protected int getFocusFlags() {
		if (focus < 0)
			return FOCUS_NEAR;
		else if (focus > 0)
			return FOCUS_FAR;
		else
			return 0;
	}

	/** Construct an int containing the iris command flags in the 2 LSBs */
	protected int getIrisFlags() {
		if (iris < 0)
			return IRIS_CLOSE;
		else if (iris > 0)
			return IRIS_OPEN;
		else
			return 0;
	}

	/** Construct an int containing the full command bytes in the 2 LSBs */
	protected int getCommandFlags() {
		return (getPanFlags() | getTiltFlags() | getZoomFlags()
			| getFocusFlags() | getIrisFlags());
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(OutputStream os, int drop) throws IOException {
		byte[] pkt = createPacket(drop);
		int cmd = getCommandFlags();
		pkt[2] = (byte)(((cmd & 0xff00) >>> 8) & 0xff);
		pkt[3] = (byte)(((cmd & 0x00ff) >>> 0) & 0xff);
		pkt[4] = (byte)Math.abs(pan);
		pkt[5] = (byte)Math.abs(tilt);
		pkt[6] = calculateChecksum(pkt);
		os.write(pkt);
	}
}
