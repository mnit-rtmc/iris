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
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;

/**
 * Manchester operation to move a camera.
 *
 * @author Douglas Lau
 */
public class OpMoveCamera extends OpManchester {

	/** Range of PTZ values */
	static protected final int PTZ_RANGE = 8;

	/** Clamp a float value to the range of (-1, 1) */
	static protected float clamp_float(float value) {
		return Math.max(-1, Math.min(value, 1));
	}

	/** Map a float value to an integer range */
	static protected int map_float(float value, int range) {
		return Math.round(clamp_float(value) * range);
	}

	/** The direction (and speed) to pan the camera */
	private final int pan;

	/** The direction (and speed) to tilt the camera */
	private final int tilt;

	/** The direction to zoom the camera */
	private final int zoom;

	/** Create a new operation to move a camera */
	public OpMoveCamera(CameraImpl c, float p, float t, float z) {
		super(c);
		pan = map_float(p, PTZ_RANGE);
		tilt = map_float(t, PTZ_RANGE);
		zoom = map_float(z, PTZ_RANGE);
	}

	/**
	 * Test whether or not the command is to stop all PTZ.
	 * @return boolean True if it is a stop command, false otherwise.
	 */
	public boolean isStopCmd() {
		return pan == 0 && tilt == 0 && zoom == 0;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<ManchesterProperty> phaseTwo() {
		return new Move();
	}

	/** Phase to move the camera */
	protected class Move extends Phase<ManchesterProperty> {

		/** Command controller to move the camera */
		protected Phase<ManchesterProperty> poll(
			CommMessage<ManchesterProperty> mess) throws IOException
		{
			sleepUntilReady();
			if (pan != 0)
				mess.add(new PanProperty(pan));
			if (tilt != 0)
				mess.add(new TiltProperty(tilt));
			if (zoom != 0)
				mess.add(new ZoomProperty(zoom));
			mess.storeProps();
			if (isStopCmd() || isExpired())
				return null;
			else
				return this;
		}
	}
}
