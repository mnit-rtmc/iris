/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.pelcod;

import java.io.IOException;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DeviceOperation;
import us.mn.state.dot.tms.server.CameraImpl;

/**
 * Pelco operation to set a camera preset.
 *
 * @author Stephen Donecker
 * @company University of California, Davis
 */
public class SetCameraPreset extends DeviceOperation {

	/** The camera preset to set */
	private final int m_preset;

	/** Create a new operation to set a camera preset */
	public SetCameraPreset(CameraImpl c, int preset) {
		super(COMMAND, c);
		m_preset = preset;
	}

	/** Begin the operation */
	public Phase phaseOne() {
		return new SetPreset();
	}

	/** Phase to set the camera preset */
	protected class SetPreset extends Phase {

		/** Command controller to set the camera preset */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new SetPresetCommandRequest(m_preset));
			mess.setRequest();
			return null;
		}
	}
}
