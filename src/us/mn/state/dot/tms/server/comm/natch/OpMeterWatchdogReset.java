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
 * Step to reset a ramp meter watchdog
 *
 * @author Douglas Lau
 */
public class OpMeterWatchdogReset extends OpStep {

	/** Message ID counter */
	private final Counter counter;

	/** Watchdog reset pin */
	private final Integer pin;

	/** Reset state */
	private boolean reset = true;

	/** Was successfully received */
	private boolean success = false;

	/** Create a new ramp meter timing step */
	public OpMeterWatchdogReset(Counter c, RampMeterImpl m) {
		counter = c;
		pin = RampMeterHelper.lookupWatchdogResetPin(m);
		if (pin == null)
			success = true;
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		if (pin != null) {
			PinStatusProp prop = new PinStatusProp(counter, pin,
				reset);
			prop.encodeStore(op, tx_buf);
			setPolling(false);
		}
	}

	/** Parse data received from controller */
	@Override
	public void recv(Operation op, ByteBuffer rx_buf) throws IOException {
		if (pin != null) {
			PinStatusProp prop = new PinStatusProp(counter, pin,
				reset);
			prop.decodeStore(op, rx_buf);
			if (reset) {
				reset = false;
				setPolling(true);
			} else
				success = true;
		}
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return success ? null : this;
	}
}
