/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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
 * Vicon operation to move a camera.
 *
 * @author Douglas Lau
 */
public class OpMoveCamera extends OpViconPTZ {

	/** Range of PTZ values */
	static private final int PTZ_RANGE = 2048;

	/** Clamp a float value to the range of (-1, 1) */
	static private float clamp_float(float value) {
		return Math.max(-1, Math.min(value, 1));
	}

	/** Map a float value to an integer range */
	static private int map_float(float value, int range) {
		return Math.round(clamp_float(value) * (range - 1));
	}

	/** Command property */
	private final CommandProperty prop;

	/** Create a new operation to move a camera */
	public OpMoveCamera(CameraImpl c, float p, float t, float z) {
		super(c);
		int pan = map_float(p, PTZ_RANGE);
		int tilt = map_float(t, PTZ_RANGE);
		int zoom = map_float(z, PTZ_RANGE);
		prop = new CommandProperty(pan, tilt, zoom, 0, 0);
	}

	/** Create the second phase of the operation */
	protected Phase<ViconPTZProperty> phaseTwo() {
		return new Move();
	}

	/** Phase to move the camera */
	protected class Move extends Phase<ViconPTZProperty> {

		/** Number of times this request was sent */
		private int n_sent = 0;

		/** Command controller to move the camera */
		protected Phase<ViconPTZProperty> poll(
			CommMessage<ViconPTZProperty> mess) throws IOException
		{
			mess.add(prop);
			mess.storeProps();
			n_sent++;
			return shouldResend() ? this : null;
		}

		/** Should we resend the property? */
		private boolean shouldResend() {
			return prop.isStop() && (n_sent < 2);
		}
	}
}
