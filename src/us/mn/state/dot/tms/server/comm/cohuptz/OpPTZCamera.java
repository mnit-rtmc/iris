/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  AHMCT, University of California
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

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Cohu PTZ operation to pan/tilt/zoom a camera.
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class OpPTZCamera extends OpStep {

	/** Pan property */
	private final PanProp pan;

	/** Tilt property */
	private final TiltProp tilt;

	/** Zoom property */
	private final ZoomProp zoom;

	/**
	 * Create the operation.
	 * @param p the pan vector [-1..1]
	 * @param t the tilt vector [-1..1]
	 * @param z the zoom vector [-1..1]
	 */
	public OpPTZCamera(float p, float t, float z) {
		pan  = new PanProp(p);
		tilt = new TiltProp(t);
		zoom = new ZoomProp(z);
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		pan.encodeStore(op, tx_buf);
		tilt.encodeStore(op, tx_buf);
		zoom.encodeStore(op, tx_buf);
	}
}
