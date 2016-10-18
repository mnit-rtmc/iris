/*
 * IRIS -- Intelligent Roadway Information System
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
package us.mn.state.dot.tms.server.comm.pelcop;

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Operation step to listen for keyboard commands.
 *
 * @author Douglas Lau
 */
public class OpListenKeyboard extends OpStep {

	/** Create a new listen keyboard step */
	public OpListenKeyboard() {
		setPolling(false);
	}

	/** Parse response from controller */
	@Override
	public void resp(Operation op, ByteBuffer rx_buf) throws IOException {
		// parse data from keyboard
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return this;
	}
}
