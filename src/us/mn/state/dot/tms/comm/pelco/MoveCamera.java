/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.pelco;

import java.io.IOException;
import us.mn.state.dot.tms.CameraImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DeviceOperation;

/**
 * Pelco operation to move a camera.
 *
 * @author Douglas Lau
 */
public class MoveCamera extends DeviceOperation {

	/** The direction (and speed) to pan the camera */
	protected final int pan;

	/** The direction (and speed) to tilt the camera */
	protected final int tilt;

	/** The direction to zoom the camera */
	protected final int zoom;

	/** Create a new operation to move a camera */
	public MoveCamera(CameraImpl c, int p, int t, int z) {
		super(COMMAND, c);
		pan = p;
		tilt = t;
		zoom = z;
	}

	/** Begin the operation */
	public Phase phaseOne() {
		return new Move();
	}

	/** Phase to move the camera */
	protected class Move extends Phase {

		/** Command controller to move the camera */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new CommandRequest(pan, tilt, zoom));
			mess.setRequest();
			return null;
		}
	}
}
