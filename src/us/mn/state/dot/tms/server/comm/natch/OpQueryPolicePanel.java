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
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.server.RampMeterImpl;
import us.mn.state.dot.tms.server.comm.Operation;

/**
 * Step to query a police panel switch
 *
 * @author Douglas Lau
 */
public class OpQueryPolicePanel extends OpNatch {

	/** Ramp meter device */
	private final RampMeterImpl meter;

	/** Pin status property */
	private final PinStatusProp prop;

	/** Create a new query police panel step */
	public OpQueryPolicePanel(Counter c, RampMeterImpl m) {
		meter = m;
		Integer pin = RampMeterHelper.lookupPolicePanelPin(m);
		// Don't query if no PP pin for the cabinet style
		if (pin == null)
			setDone(true);
		prop = new PinStatusProp(c, (pin != null) ? pin : 0);
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		if (!done) {
			prop.encodeQuery(op, tx_buf);
			setPolling(false);
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
		meter.setPolicePanel(prop.getStatus());
		setDone(true);
	}
}
