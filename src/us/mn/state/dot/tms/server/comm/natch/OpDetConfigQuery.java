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
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Step to query a detector configuration
 *
 * @author Douglas Lau
 */
public class OpDetConfigQuery extends OpStep {

	/** Message ID counter */
	private final Counter counter;

	/** Detector config property */
	private final DetectorConfigProp prop;

	/** Is store needed? */
	private boolean store = false;

	/** Was successfully received */
	private boolean success = false;

	/** Create a new detector config query step */
	public OpDetConfigQuery(Counter c, int dn) {
		counter = c;
		prop = new DetectorConfigProp(c, dn);
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		if (store)
			prop.encodeStore(op, tx_buf);
		else
			prop.encodeQuery(op, tx_buf);
		setPolling(false);
	}

	/** Parse data received from controller */
	@Override
	public void recv(Operation op, ByteBuffer rx_buf) throws IOException {
		prop.decodeQuery(op, rx_buf);
		if (prop.isPinCorrect(op))
			success = true;
		else {
			store = true;
			setPolling(true);
		}
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		if (success) {
			int dn = prop.detector_num + 1;
			return (dn < 32)
			      ? new OpDetConfigQuery(counter, dn)
			      : null;
		} else
			return this;
	}
}
