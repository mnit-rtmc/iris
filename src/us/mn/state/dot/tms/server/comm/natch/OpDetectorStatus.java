/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
 * Step to listen for detector status
 *
 * @author Douglas Lau
 */
public class OpDetectorStatus extends OpNatch {

	/** Detector status property */
	private final DetectorStatusProp prop;

	/** Create a new step to listen for detector status */
	public OpDetectorStatus(Counter c) {
		prop = new DetectorStatusProp(c);
		setPolling(false);
	}

	/** Clear a received error */
	@Override
	public void clearError() {
		// DS not polled, wait for more messages
		setPolling(false);
	}

	/** Poll the controller.
	 *
	 * For `ds` messages, this serves as an ACK */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		prop.encodeQuery(op, tx_buf);
		setPolling(false);
	}

	/** Is this step waiting indefinitely */
	@Override
	public boolean isWaitingIndefinitely() {
		return !isPolling();
	}

	/** Parse data received from controller */
	@Override
	public void recv(Operation op, ByteBuffer rx_buf) throws IOException {
		prop.decodeQuery(op, rx_buf);
		prop.logEvent(op);
		setPolling(true);
	}
}
