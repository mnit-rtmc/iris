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
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Step to send ramp meter status
 *
 * @author Douglas Lau
 */
public class OpSendMeterStatus extends OpNatch {

	/** Ramp meter device */
	private final RampMeterImpl meter;

	/** Meter status property */
	private final MeterStatusProp prop;

	/** Create a new send meter status step */
	public OpSendMeterStatus(Counter c, RampMeterImpl m, Integer rate) {
		meter = m;
		prop = new MeterStatusProp(c, meter);
		int red = (rate != null) ? RedTime.fromReleaseRate(rate) : 0;
		prop.setRed(red);
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
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
		assert pr == prop;
		Integer red = prop.getRed();
		Integer rate = (red != null && red > 0)
			? RedTime.toReleaseRate(red)
			: null;
		meter.setRateNotify(rate);
		setDone(true);
	}
}
