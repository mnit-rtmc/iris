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
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Step to configure a ramp meter
 *
 * @author Douglas Lau
 */
public class OpMeterConfigure extends OpNatch {

	/** Message ID counter */
	private final Counter counter;

	/** Ramp meter */
	private final RampMeterImpl meter;

	/** Meter config property */
	private final MeterConfigProp prop;

	/** Create a new configure ramp meter step */
	public OpMeterConfigure(Counter c, RampMeterImpl m) {
		counter = c;
		meter = m;
		prop = new MeterConfigProp(c, meter);
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
		setDone(true);
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return done ? new OpMeterTiming(counter, meter, 0) : this;
	}
}
