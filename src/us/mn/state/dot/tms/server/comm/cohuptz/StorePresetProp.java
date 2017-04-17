/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * This class creates a Cohu PTZ request to instruct a camera
 * to store the current state to a specified preset.
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class StorePresetProp extends CohuPTZProp {

	/** Requested preset to store */
	private final int preset;

	/** Create a new store preset property */
	public StorePresetProp(int p) {
		preset = p;
	}

	/** Get the property comand */
	@Override
	protected byte[] getCommand() throws ProtocolException {
		byte[] cmd = new byte[2];
		cmd[0] = (byte) 'P';
		cmd[1] = getPresetByte(preset);
		return cmd;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "store preset: " + preset;
	}
}
