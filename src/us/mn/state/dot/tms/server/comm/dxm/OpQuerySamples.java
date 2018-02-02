/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dxm;

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * DXM operation to query sample data.
 *
 * @author Douglas Lau
 */
public class OpQuerySamples extends OpStep {

	/** Sample property */
	private final SampleProp prop;

	/** Flag to do step again */
	private boolean again;

	/** Create a new query samples operation */
	public OpQuerySamples(int p) {
		prop = new SampleProp(p);
		again = true;
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		prop.encodeQuery(op, tx_buf);
		setPolling(false);
	}

	/** Parse data received from controller */
	@Override
	public void recv(Operation op, ByteBuffer rx_buf) throws IOException {
		prop.decodeQuery(op, rx_buf);
		again = false;
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return again ? this : null;
	}
}
