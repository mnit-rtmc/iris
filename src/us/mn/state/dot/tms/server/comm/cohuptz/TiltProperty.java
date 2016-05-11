/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
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
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * A property to tilt a camera
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class TiltProperty extends CohuPTZProperty {

	/** Requested vector [-1..1] */
	private final float value;

	/** Create the property */
	public TiltProperty(float v) {
		value = v;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] cmd = new byte[2];
		if (Math.abs(value) < PTZ_THRESH) {
			cmd[0] = (byte) 'T';	// tilt
			cmd[1] = (byte) 'S';	// stop
		} else if (value < 0) {
			cmd[0] = (byte) 'd';	// down
			cmd[1] = getPanTiltSpeedByte(value);
		} else {
			cmd[0] = (byte) 'u';	// up
			cmd[1] = getPanTiltSpeedByte(value);
		}
		os.write(createPacket(c.getDrop(), cmd));
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "tilt: " + value;
	}
}
