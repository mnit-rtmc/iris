/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2022  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.NotReceivedException;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Step for Natch operations
 *
 * @author Douglas Lau
 */
abstract public class OpNatch extends OpStep {

	/** Is step done? */
	protected boolean done;

	/** Set the step to be done */
	public void setDone(boolean d) {
		done = d;
	}

	/** Create a new Natch step */
	protected OpNatch() {
		done = false;
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return done ? null : this;
	}

	/** Parse data received from controller */
	@Override
	public void recv(Operation op, ByteBuffer rx_buf) throws IOException {
		boolean received = false;
		byte[] buf = new byte[rx_buf.remaining()];
		rx_buf.get(buf);
		String msgs = new String(buf, NatchProp.UTF8);
		for (String msg : msgs.split("\n")) {
			NatchProp prop = getProp();
			if (prop.parseMsg(msg)) {
				handleReceived(op, prop);
				received = true;
			}
		}
		if (!received)
			throw new NotReceivedException();
	}

	/** Get the property */
	abstract protected NatchProp getProp();

	/** Handle received property */
	protected void handleReceived(Operation op, NatchProp prop) {
		// can be overridden
	}
}
