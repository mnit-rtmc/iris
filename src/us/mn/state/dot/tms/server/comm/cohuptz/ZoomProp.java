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
 * A property to zoom a camera
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class ZoomProp extends CohuPTZProp {

	/** Requested vector [-1..1] */
	private final float value;

	/** Create the property */
	public ZoomProp(float v) {
		value = v;
	}

	/** Get the property comand */
	@Override
	protected byte[] getCommand() {
		if (Math.abs(value) < PTZ_THRESH) {
			byte[] cmd = new byte[2];
			cmd[0] = (byte) 'Z';	// zoom
			cmd[1] = (byte) 'S';	// stop
			return cmd;
		} else if (value < 0) {
			byte[] cmd = new byte[3];
			cmd[0] = (byte) 'c';
			cmd[1] = (byte) 'z';	// zoom wide
			cmd[2] = getZoomSpeedByte(value);
			return cmd;
		} else {
			byte[] cmd = new byte[3];
			cmd[0] = (byte) 'c';
			cmd[1] = (byte) 'Z';	// zoom tele
			cmd[2] = getZoomSpeedByte(value);
			return cmd;
		}
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "zoom: " + value;
	}
}
