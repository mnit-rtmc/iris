/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Vicon operation to move a camera.
 *
 * @author Douglas Lau
 */
public class OpMoveCamera extends OpStep {

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

	/** Pan value */
	private final int pan;

	/** Tilt value */
	private final int tilt;

	/** Zoom value */
	private final int zoom;

	/** Create a new operation to move a camera */
	public OpMoveCamera(float p, float t, float z) {
		pan = map_float(p, PTZ_RANGE);
		tilt = map_float(t, PTZ_RANGE);
		zoom = map_float(z, PTZ_RANGE);
	}

	/** Number of times this request was sent */
	private int n_sent = 0;

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		CommandProp prop = new CommandProp(op.getDrop(), pan, tilt,
			zoom, 0, 0);
		prop.encodeStore(tx_buf);
		n_sent++;
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return shouldResend() ? this : null;
	}

	/** Should we resend the property? */
	private boolean shouldResend() {
		return isStop() && (n_sent < 2);
	}

	/** Is this a stop command? */
	private boolean isStop() {
		return (pan == 0) && (tilt == 0) && (zoom == 0);
	}
}
