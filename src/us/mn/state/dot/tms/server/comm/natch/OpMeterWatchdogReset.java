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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Step to reset a ramp meter watchdog
 *
 * @author Douglas Lau
 */
public class OpMeterWatchdogReset extends OpNatch {

	/** Message ID counter */
	private final Counter counter;

	/** Watchdog reset pin */
	private final Integer pin;

	/** Pin status property */
	private final PinStatusProp prop;

	/** Create a new ramp meter timing step */
	public OpMeterWatchdogReset(Counter c, RampMeterImpl m) {
		counter = c;
		pin = RampMeterHelper.lookupWatchdogResetPin(m);
		prop = new PinStatusProp(c, (pin != null) ? pin : 0, true);
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		if (pin != null) {
			prop.encodeStore(op, tx_buf);
			setPolling(false);
		} else {
			op.putCtrlFaults("other", "Cabinet style not set");
			setDone(true);
		}
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
		if (pin != null) {
			if (prop.getStatus()) {
				// FIXME: add "waiting" state to OpStep so that
				//        we don't sleep on BasePoller thread
				TimeSteward.sleep_well(100);
				prop.setStatus(false);
				setPolling(true);
			} else
				setDone(true);
		} else {
			op.putCtrlFaults("other", "Cabinet style not set");
			setDone(true);
		}
	}
}
