/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2012  Minnesota Department of Transportation
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

	/** Operation timeout in miliseconds */
	static private final int OP_TIMEOUT_MS = 30000;

	/** The number of milliseconds between commands */
	static private final int CMD_INTERVAL_MS = 60;

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

	/** Time stamp to send phase */
	private long stamp = TimeSteward.currentTimeMillis();

	/** Check if operaton should be sent */
	protected boolean shouldSend() {
		long now = TimeSteward.currentTimeMillis();
		if(stamp < now)
			return false;
		else {
			stamp = now + CMD_INTERVAL_MS;
			should_sleep = false;
			return true;
		}
	}

	/** Flag to enable sleeping */
	private boolean should_sleep = false;

	/** Delay a bit.  We only sleep if this is called twice without sending.
	 * This forces the operation to cycle through the queue twice, allowing
	 * any other operatons to run. */
	protected void delay() {
		if(should_sleep)
			sleepBriefly();
		else
			should_sleep = true;
	}

	/** Sleep briefly */
	private void sleepBriefly() {
		long now = TimeSteward.currentTimeMillis();
		long ms = Math.min(stamp - now, 10);
		if(ms > 0) {
			try {
				Thread.sleep(ms);
			}
			catch(InterruptedException e) {
				// Nothing to do here
			}
		}
	}
}
