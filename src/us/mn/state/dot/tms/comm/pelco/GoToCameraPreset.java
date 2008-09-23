/*
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
import us.mn.state.dot.tms.comm.Device2Operation;

/**
 * Pelco operation to goto a camera preset.
 *
 * @author Stephen Donecker
 * @company University of California, Davis
 * @created July 2, 2008
 */
public class GoToCameraPreset extends Device2Operation {

	/** The camera preset to goto */
	private final int m_preset;

	/** Create a new operation to goto a camera preset */
	public GoToCameraPreset(CameraImpl c, int preset) {
		super(COMMAND, c);
		m_preset = preset;
	}

	/** Begin the operation */
	public Phase phaseOne() {
		return new GoToPreset();
	}

	/** Phase to set the camera preset */
	protected class GoToPreset extends Phase {

		/** Command controller to set the camera preset */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new GoToPresetCommandRequest(m_preset));
			mess.setRequest();
			return null;
		}
	}
}
