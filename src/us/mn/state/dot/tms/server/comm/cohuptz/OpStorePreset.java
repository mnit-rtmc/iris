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
 * Cohu PTZ operation to store a camera preset.
 *
 * @author Travis Swanston
 */
public class OpStorePreset extends OpDevice {

	/** The camera preset to store */
	private final int preset;

	/** Create a new operation to store a camera preset */
	public OpStorePreset(CameraImpl c, int p) {
		super(PriorityLevel.COMMAND, c);
		preset = p;
	}

	/** Begin the operation */
	@Override
	public Phase phaseTwo() {
		return new StorePreset();
	}

	/** Phase to store a camera preset */
	protected class StorePreset extends Phase {
		/** Command controller to store the camera preset */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(new StorePresetProperty(preset));
			mess.storeProps();
			return null;
		}
	}

}
