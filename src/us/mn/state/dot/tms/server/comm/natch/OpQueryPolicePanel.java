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
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Step to query a police panel switch
 *
 * @author Douglas Lau
 */
public class OpQueryPolicePanel extends OpStep {

	/** Ramp meter device */
	private final RampMeterImpl meter;

	/** Pin status property */
	private final PinStatusProp prop;

	/** Was successfully received */
	private boolean success = false;

	/** Create a new query police panel step */
	public OpQueryPolicePanel(Counter c, RampMeterImpl m) {
		meter = m;
		Integer pin = RampMeterHelper.lookupPolicePanelPin(m);
		prop = new PinStatusProp(c, (pin != null) ? pin : 0);
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
		meter.setPolicePanel(prop.getStatus());
		success = true;
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return success ? null : this;
	}
}
