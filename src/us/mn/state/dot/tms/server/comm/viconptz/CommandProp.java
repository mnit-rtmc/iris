/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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

/**
 * A property to command a camera.
 *
 * @author Douglas Lau
 */
public class CommandProp extends ExtendedProp {

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

	/** Requested pan [-2047 to 2047] :: [left, right] */
	private final int pan;

	/** Requested tilt [-2047 to 2047] :: [down, up] */
	private final int tilt;

	/** Requested zoom [-2047, 2047] :: [out, in] */
	private final int zoom;

	/** Requested focus value [-1, 1] :: [near, far] */
	private final int focus;

	/** Requested iris value [-1, 1] :: [close, open] */
	private final int iris;

	/** Create a new command property */
	public CommandProp(int d, int p, int t, int z, int f, int i)
		throws IOException
	{
		super(d);
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

	/** Get pan/tilt flags */
	@Override
	protected byte panTiltFlags() {
		return (byte) (panFlags() | tiltFlags());
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
	@Override
	protected byte lensFlags() {
		return (byte) (irisFlags() | focusFlags() | zoomFlags());
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

	/** Get command parameter 1 */
	@Override
	protected int getParam1() {
		return Math.abs(pan);
	}

	/** Get command parameter 2 */
	@Override
	protected int getParam2() {
		return Math.abs(tilt);
	}
}
