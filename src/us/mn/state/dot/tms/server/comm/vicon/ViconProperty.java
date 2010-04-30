/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.vicon;

import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * Vicon Property
 *
 * @author Douglas Lau
 */
abstract public class ViconProperty extends ControllerProperty {

	/** Value to indicate no selected camera */
	static protected final int CAMERA_NONE = -1;

	/** Command for getting the current camera */
	static protected final String COMMAND_GET_CAMERA = "f";

	/** Command for starting a tour */
	static protected final String COMMAND_START_TOUR = "C";

	/** Command for getting the current tour */
	static protected final String COMMAND_GET_CURRENT_TOUR = "j";

	/** Minimum value allowed for a request */
	static protected final int MIN_VALUE = -256;

	/** Maximum value allowed for a request */
	static protected final int MAX_VALUE = 256;

	/** Clamp a value to within the allowed range */
	static protected int clampValue(int v) {
		return Math.max(Math.min(v, MAX_VALUE), MIN_VALUE);
	}

	/** Encode a STORE request */
	public void encodeStore(OutputStream os, int drop) throws IOException {
		os.write(toString().getBytes());
	}
}
