/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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

/**
 * Pelco operation to recall or store a camera preset.
 *
 * @author Douglas Lau
 */
public class OpPreset extends OpPelcoD {

	/** Get recall or store property command.
	 * @param s Store or recall.
	 * @return Extended property command. */
	static private ExtendedProperty.Command getCommand(boolean s) {
		return (s) ? ExtendedProperty.Command.STORE_PRESET
		           : ExtendedProperty.Command.RECALL_PRESET;
	}

	/** Property for preset command */
	private final PelcoDProperty prop;

	/** Create a new operation for a camera preset */
	public OpPreset(CameraImpl c, boolean s, int p) {
		super(c);
		prop = new ExtendedProperty(getCommand(s), p);
	}

	/** Create the second phase of the operation */
	protected Phase<PelcoDProperty> phaseTwo() {
		return new CommandPreset();
	}

	/** Phase to recall or store a camera preset */
	protected class CommandPreset extends Phase<PelcoDProperty> {

		/** Command controller to recall or store a preset */
		protected Phase<PelcoDProperty> poll(
			CommMessage<PelcoDProperty> mess) throws IOException
		{
			mess.add(prop);
			mess.storeProps();
			return null;
		}
	}
}
