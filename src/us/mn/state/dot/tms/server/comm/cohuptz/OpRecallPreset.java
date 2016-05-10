/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  AHMCT, University of California
 * Copyright (C) 2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Cohu PTZ operation to recall a camera preset.
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class OpRecallPreset extends OpCohuPTZ {

	/** Preset number */
	private final int preset;

	/**
	 * Create a new operation to recall a camera preset.
	 * @param c the CameraImpl instance
	 * @param cp the CohuPTZPoller instance
	 * @param p the preset number to recall
	 */
	public OpRecallPreset(CameraImpl c, CohuPTZPoller cp, int p) {
		super(PriorityLevel.COMMAND, c, cp);
		preset = p;
	}

	/** Begin the operation */
	@Override
	protected Phase<CohuPTZProperty> phaseTwo() {
		return new RecallPreset();
	}

	/** Phase to recall a camera preset */
	protected class RecallPreset extends Phase<CohuPTZProperty> {
		protected Phase<CohuPTZProperty> poll(
			CommMessage<CohuPTZProperty> mess)
			throws IOException
		{
			mess.add(new RecallPresetProperty(preset));
			doStoreProps(mess);
			return null;
		}
	}
}
