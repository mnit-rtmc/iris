/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
 * Pelco operation to recall a camera preset.
 *
 * @author Stephen Donecker
 * @author Douglas Lau
 */
public class OpRecallPreset extends OpPelcoD {

	/** The camera preset to goto */
	private final int preset;

	/** Create a new operation to recall a camera preset */
	public OpRecallPreset(CameraImpl c, int p) {
		super(c);
		preset = p;
	}

	/** Create the second phase of the operation */
	protected Phase<PelcoDProperty> phaseTwo() {
		return new RecallPreset();
	}

	/** Phase to recall a camera preset */
	protected class RecallPreset extends Phase<PelcoDProperty> {

		/** Command controller to set the camera preset */
		protected Phase<PelcoDProperty> poll(
			CommMessage<PelcoDProperty> mess) throws IOException
		{
			mess.add(new ExtendedProperty(ExtendedProperty.
				Command.RECALL_PRESET, preset));
			mess.storeProps();
			return null;
		}
	}
}
