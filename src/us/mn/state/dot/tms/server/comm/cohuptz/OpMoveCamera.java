/*
 * IRIS -- Intelligent Roadway Information System
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

package us.mn.state.dot.tms.server.comm.cohuptz;

import java.io.IOException;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Cohu PTZ operation to move a camera.
 *
 * @author Travis Swanston
 */
public class OpMoveCamera extends OpDevice {

	/** The direction (and speed) to pan the camera */
	protected final float pan;

	/** The direction (and speed) to tilt the camera */
	protected final float tilt;

	/** The direction to zoom the camera */
	protected final float zoom;

	/** Create a new operation to move a camera */
	public OpMoveCamera(CameraImpl c, float p, float t, float z) {
		super(PriorityLevel.COMMAND, c);
		pan = p;
		tilt = t;
		zoom = z;
	}

	/** Begin the operation */
	@Override
	public Phase phaseTwo() {
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
