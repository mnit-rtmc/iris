/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2018  Minnesota Department of Transportation
 * Copyright (C) 2018  SRF Consulting Group
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
 * A combined property to pan and/or tilt a Helios camera.
 *
 * @author Douglas Lau
 * @author Michael Janson
 */
public class PanTiltZoomProp extends CohuPTZProp {

	/** Pan property */
	private final PanProp pan;

	/** Tilt property */
	private final TiltProp tilt;

	/** Zoom property */
	private final ZoomProp zoom;

	/** Create the property */
	public PanTiltZoomProp(PanProp p, TiltProp t, ZoomProp z) {
		pan = p;
		tilt = t;
		zoom = z;
	}

	/** Get the property comand */
	@Override
	protected byte[] getCommand() {
		byte[] pan_command = pan.getCommand();
		byte[] tilt_command = tilt.getCommand();
		byte[] zoom_command = zoom.getCommand();

		byte[] cmd = new byte[pan_command.length + tilt_command.length +
			zoom_command.length];
		System.arraycopy(pan_command, 0, cmd, 0, pan_command.length);
		System.arraycopy(tilt_command, 0, cmd, pan_command.length,
			tilt_command.length);
		System.arraycopy(zoom_command, 0, cmd, pan_command.length +
			tilt_command.length, zoom_command.length);
		return cmd;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return pan.toString() + ", " + tilt.toString();
	}
}
