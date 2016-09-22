/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Vicon operation to recall or store a camera preset.
 *
 * @author Douglas Lau
 * @author Stephen Donecker
 */
public class OpPreset extends OpStep {

	/** Special preset for on-screen menu (store) */
	static private final int PRESET_MENU = 8;

	/** Beginning of extended preset block */
	static private final int PRESET_EXT = 21;

	/** Adjust a preset number.  This is needed for Pelco cameras with a
	 * TXB-V translator board installed.  In this case, presets 8 through
	 * 20 are special functions, and cannot be used as intended.  For
	 * example, storing preset 8 brings up the on-screen menu.  So, presets
	 * 8-12 (in IRIS) use 21-25 within the camera.
	 * @param p Preset number.
	 * @return Actual preset number to send to camera. */
	static private int adjustPreset(int p) {
		return (p < PRESET_MENU)
		      ? p
		      : p - PRESET_MENU + PRESET_EXT;
	}

	/** Store / recall flag */
	private final boolean store;

	/** Preset number */
	private final int preset;

	/** Create a new operation to recall or store a camera preset */
	public OpPreset(boolean s, int p) {
		store = s;
		preset = p;
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		ExPresetProp prop = new ExPresetProp(op.getDrop(), store,
			adjustPreset(preset));
		prop.encodeStore(tx_buf);
	}
}
