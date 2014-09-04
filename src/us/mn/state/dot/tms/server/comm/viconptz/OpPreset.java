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
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;

/**
 * Vicon operation to recall or store a camera preset.
 *
 * @author Douglas Lau
 * @author Stephen Donecker
 */
public class OpPreset extends OpViconPTZ {

	/** Store (or recall) */
	private final boolean store;

	/** Camera preset to reall or store */
	private final int preset;

	/** Create a new operation to recall or store a camera preset */
	public OpPreset(CameraImpl c, boolean s, int p) {
		super(c);
		store = s;
		preset = p;
	}

	/** Create the second phase of the operation */
	protected Phase<ViconPTZProperty> phaseTwo() {
		return new CommandPreset();
	}

	/** Phase to recall or store a camera preset */
	protected class CommandPreset extends Phase<ViconPTZProperty> {

		/** Command controller to recall or store a preset */
		protected Phase<ViconPTZProperty> poll(
			CommMessage<ViconPTZProperty> mess) throws IOException
		{
			mess.add(new PresetProperty(store, preset));
			mess.storeProps();
			return null;
		}
	}
}
