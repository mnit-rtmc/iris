/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.manchester;

import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Manchester operation to move a camera.
 *
 * @author Douglas Lau
 */
abstract public class OpManchester extends OpDevice<ManchesterProperty> {

	/** Operation timeout in milliseconds */
	static private final int OP_TIMEOUT_MS = 30000;

	/** The number of milliseconds between commands */
	static private final int CMD_INTERVAL_MS = 50;

	/** Time stamp when operation will expire */
	private final long expire;

	/** Check if the operation has expired */
	protected boolean isExpired() {
		return TimeSteward.currentTimeMillis() >= expire;
	}

	/** Create a new manchester operation */
	protected OpManchester(CameraImpl c) {
		super(PriorityLevel.COMMAND, c);
		expire = TimeSteward.currentTimeMillis() + OP_TIMEOUT_MS;
	}

	/** Time stamp when ready to send */
	private long ready = TimeSteward.currentTimeMillis();

	/** Sleep until we're ready to send */
	protected void sleepUntilReady() {
		long now = TimeSteward.currentTimeMillis();
		long ms = Math.min(ready - now, CMD_INTERVAL_MS);
		if (ms > 0)
			TimeSteward.sleep_well(ms);
		ready = now + CMD_INTERVAL_MS;
	}
}
