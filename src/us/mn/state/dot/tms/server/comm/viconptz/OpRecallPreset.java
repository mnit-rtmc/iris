/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;

/**
 * Vicon operation to recall a camera preset.
 *
 * @author Stephen Donecker
 */
public class OpRecallPreset extends OpDevice {

	/** The camera preset to recall */
	private final int m_preset;

	/** Create a new operation to recall a camera preset */
	public OpRecallPreset(CameraImpl c, int preset) {
		super(COMMAND, c);
		m_preset = preset;
	}

	/** Begin the operation */
	public Phase phaseOne() {
		return new RecallPreset();
	}

	/** Phase to recall the camera preset */
	protected class RecallPreset extends Phase {

		/** Command controller to recall the camera preset */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(new RecallPresetProperty(m_preset));
			mess.setRequest();
			return null;
		}
	}
}
