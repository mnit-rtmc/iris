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

/**
 * A property to command a camera
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class CommandProperty extends PelcoDProperty {

	/** Bit flag to command a pan right */
	static private final byte PAN_RIGHT = 1 << 1;

	/** Bit flag to command a pan left */
	static private final byte PAN_LEFT = 1 << 2;

	/** Bit flag to command a tilt up */
	static private final byte TILT_UP = 1 << 3;

	/** Bit flag to command a tilt down */
	static private final byte TILT_DOWN = 1 << 4;

	/** Bit flag to command a zoom in */
	static private final byte ZOOM_IN = 1 << 5;

	/** Bit flag to command a zoom out */
	static private final byte ZOOM_OUT = 1 << 6;

	/** Bit flag to command a focus-far op */
	static private final int FOCUS_FAR = 1 << 7;

	/** Bit flag to command a focus-near op */
	static private final int FOCUS_NEAR = 1 << 8;

	/** Bit flag to command an iris-open op */
	static private final int IRIS_OPEN = 1 << 9;

	/** Bit flag to command an iris-close op */
	static private final int IRIS_CLOSE = 1 << 10;

	/** Bit flag for sense 0 */
	static private final int SENSE_0 = 1 << 11;

	/** Bit flag for sense 1 */
	static private final int SENSE_1 = 1 << 12;

	/** Bit flag for sense 2 */
	static private final int SENSE_2 = 1 << 15;

	/** Requested pan value [-63, 63] :: [left, right] (64 is turbo) */
	private final int pan;

	/** Requested tilt value [-63, 63] :: [down, up] */
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

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "ptz: " + pan + "," + tilt + "," + zoom + " focus:" +
			focus + " iris:" + iris;
	}

	/** Is this a stop command? */
	public boolean isStop() {
		return pan == 0 && tilt == 0 && zoom == 0
		    && focus == 0 && iris == 0;
	}

	/** Construct an int containing the pan command flags in the 2 LSBs */
	private byte getPanFlags() {
		if (pan < 0)
			return PAN_LEFT;
		else if (pan > 0)
			return PAN_RIGHT;
		else
			return 0;
	}

	/** Construct an int containing the tilt command flags in the 2 LSBs */
	private byte getTiltFlags() {
		if (tilt < 0)
			return TILT_DOWN;
		else if (tilt > 0)
			return TILT_UP;
		else
			return 0;
	}

	/** Construct an int containing the zoom command flags in the 2 LSBs */
	private byte getZoomFlags() {
		if (zoom < 0)
			return ZOOM_OUT;
		else if (zoom > 0)
			return ZOOM_IN;
		else
			return 0;
	}

	/** Construct an int containing the focus command flags in the 2 LSBs */
	private int getFocusFlags() {
		if (focus < 0)
			return FOCUS_NEAR;
		else if (focus > 0)
			return FOCUS_FAR;
		else
			return 0;
	}

	/** Construct an int containing the iris command flags in the 2 LSBs */
	private int getIrisFlags() {
		if (iris < 0)
			return IRIS_CLOSE;
		else if (iris > 0)
			return IRIS_OPEN;
		else
			return 0;
	}

	/** Get the command bits (in the 2 LSBs) */
	@Override
	protected int getCommand() {
		return getPanFlags()
		     | getTiltFlags()
		     | getZoomFlags()
		     | getFocusFlags()
		     | getIrisFlags();
	}

	/** Get command parameter 1 */
	@Override
	protected int getParam1() {
		return Math.abs(tilt);
	}

	/** Get command parameter 2 */
	@Override
	protected int getParam2() {
		return Math.abs(pan);
	}
}
