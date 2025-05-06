/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.natch;

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Step to send clock status
 *
 * @author Douglas Lau
 */
public class OpClockStatus extends OpNatch {

	/** Clock status property */
	private final ClockStatusProp prop;

	/** Create a new clock status step */
	public OpClockStatus(Counter c) {
		prop = new ClockStatusProp(c);
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		op.putCtrlFaults(null, null);
		prop.encodeStore(op, tx_buf);
		setPolling(false);
	}

	/** Get the property */
	@Override
	protected NatchProp getProp() {
		return prop;
	}

	/** Handle received property */
	@Override
	protected void handleReceived(Operation op, NatchProp pr) {
		setDone(true);
	}
}
