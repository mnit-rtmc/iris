/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Manchester operation to move a camera.
 *
 * @author Douglas Lau
 */
public class OpMoveCamera extends OpDevice {

	/** Operation timeout in miliseconds */
	static protected final int OP_TIMEOUT_MS = 30000;

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
	protected final int pan;

	/** The direction (and speed) to tilt the camera */
	protected final int tilt;

	/** The direction to zoom the camera */
	protected final int zoom;

	/** Time stamp when operation will expire */
	public final long expire;

	/** Create a new operation to move a camera */
	public OpMoveCamera(CameraImpl c, float p, float t, float z) {
		super(PriorityLevel.COMMAND, c);
		pan = map_float(p, PTZ_RANGE);
		tilt = map_float(t, PTZ_RANGE);
		zoom = map_float(z, PTZ_RANGE);
		expire = TimeSteward.currentTimeMillis() + OP_TIMEOUT_MS;
	}

	/**
	 * Test whether or not the command is to stop all PTZ.
	 * @return boolean True if it is a stop command, false otherwise.
	 */
	public boolean isStopCmd() {
		return pan == 0 && tilt == 0 && zoom == 0;
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return new Move();
	}

	/** Phase to move the camera */
	protected class Move extends Phase {

		/** Command controller to move the camera */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(new CommandProperty(pan, tilt, zoom));
			mess.storeProps();
			return null;
		}
	}
}
