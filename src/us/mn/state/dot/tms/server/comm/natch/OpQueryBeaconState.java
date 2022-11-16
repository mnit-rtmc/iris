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
import us.mn.state.dot.tms.BeaconState;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Step to query a beacon
 *
 * @author Douglas Lau
 */
public class OpQueryBeaconState extends OpNatch {

	/** Beacon device */
	private final BeaconImpl beacon;

	/** Relay pin status property */
	private final PinStatusProp relay;

	/** Verify pin status property */
	private final PinStatusProp verify;

	/** Flag when pin query is done */
	private boolean pin_done;

	/** Create a new query beacon state step */
	public OpQueryBeaconState(Counter c, BeaconImpl b) {
		beacon = b;
		relay = new PinStatusProp(c, beacon.getPin());
		Integer vp = beacon.getVerifyPin();
		verify = (vp != null) ? new PinStatusProp(c, vp) : null;
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		if (pin_done)
			verify.encodeQuery(op, tx_buf);
		else
			relay.encodeQuery(op, tx_buf);
		setPolling(false);
	}

	/** Get the property */
	@Override
	protected NatchProp getProp() {
		return (pin_done) ? verify : relay;
	}

	/** Handle received property */
	@Override
	protected void handleReceived(Operation op, NatchProp pr) {
		if (pr == relay && verify != null)
			pin_done = true;
		else
			setDone(true);
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		if (done) {
			BeaconState bs = beacon.getBeaconState(relay.getStatus(),
				verify.getStatus());
			beacon.setStateNotify(bs);
			return null;
		} else
			return this;
	}
}
