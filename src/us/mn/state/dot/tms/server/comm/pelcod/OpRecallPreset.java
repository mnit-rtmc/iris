/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.OpDevice;

/**
 * Pelco operation to recall a camera preset.
 *
 * @author Stephen Donecker
 * @author Douglas Lau
 */
public class OpRecallPreset extends OpDevice {

	/** The camera preset to goto */
	private final int preset;

	/** Create a new operation to recall a camera preset */
	public OpRecallPreset(CameraImpl c, int p) {
		super(COMMAND, c);
		preset = p;
	}

	/** Begin the operation */
	public Phase phaseOne() {
		return new RecallPreset();
	}

	/** Phase to recall a camera preset */
	protected class RecallPreset extends Phase {

		/** Command controller to set the camera preset */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(new RecallPresetProperty(preset));
			mess.setRequest();
			return null;
		}
	}
}
