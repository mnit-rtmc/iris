/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;

/**
 * Pelco operation to move a camera.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class OpMoveCamera extends OpPelcoD {

	/** Range of PTZ values */
	static private final int PTZ_RANGE = 64;

	/** Clamp a float value to the range of (-1, 1) */
	static private float clamp_float(float value) {
		return Math.max(-1, Math.min(value, 1));
	}

	/** Map a float value to an integer range */
	static private int map_float(float value, int range) {
		return Math.round(clamp_float(value) * (range - 1));
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

	/** Create the second phase of the operation */
	protected Phase<PelcoDProperty> phaseTwo() {
		return new Move();
	}

	/** Phase to move the camera */
	protected class Move extends Phase<PelcoDProperty> {
		/** Command controller to move the camera */
		protected Phase<PelcoDProperty> poll(
			CommMessage<PelcoDProperty> mess) throws IOException
		{
			mess.add(new CommandProperty(pan, tilt, zoom, 0, 0));
			mess.storeProps();
			return null;
		}
	}

}
