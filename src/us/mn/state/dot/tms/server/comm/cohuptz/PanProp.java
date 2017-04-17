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

/**
 * A property to pan a camera
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class PanProp extends CohuPTZProp {

	/** Requested vector [-1..1] */
	private final float value;

	/** Create the property */
	public PanProp(float v) {
		value = v;
	}

	/** Get the property comand */
	@Override
	protected byte[] getCommand() {
		byte[] cmd = new byte[2];
		if (Math.abs(value) < PTZ_THRESH) {
			cmd[0] = (byte) 'P';	// pan
			cmd[1] = (byte) 'S';	// stop
		} else if (value < 0) {
			cmd[0] = (byte) 'l';	// left
			cmd[1] = getPanTiltSpeedByte(value);
		} else {
			cmd[0] = (byte) 'r';	// right
			cmd[1] = getPanTiltSpeedByte(value);
		}
		return cmd;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "pan: " + value;
	}
}
