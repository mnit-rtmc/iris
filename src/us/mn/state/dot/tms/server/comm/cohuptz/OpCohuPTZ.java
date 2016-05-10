/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  AHMCT, University of California
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cohuptz;

import java.io.IOException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Cohu PTZ operation.
 *
 * @author Travis Swanston
 */
abstract public class OpCohuPTZ extends OpDevice<CohuPTZProperty> {

	/** Minimum time interval (ms) to enforce between Cohu commands */
	static private final int MIN_CMD_INTERVAL_MS = 25;

	/** Poller */
	protected final CohuPTZPoller poller;

	/** Operation description */
	private final String op_desc;

	/**
	 * Create the operation.
	 * @param pl the operation's PriorityLevel
	 * @param c the CameraImpl instance
	 * @param c the CohuPTZPoller instance
	 * @param d the op description
	 */
	public OpCohuPTZ(PriorityLevel pl, CameraImpl ci, CohuPTZPoller cp,
		String d)
	{
		super(pl, ci);
		poller = cp;
		op_desc = d;
		device.setOpStatus("sending cmd");
	}

	/** Return operation description */
	@Override
	public String getOperationDescription() {
		return (op_desc == null ? "" : op_desc);
	}

	/**
	 * Update device op status.
	 * We bundle the operation description into the status because camera
	 * ops are generally so short that, as far as I can tell, by the time
	 * the client gets the SONAR "operation" notification and requests the
	 * op's description via SONAR, the device has already been released,
	 * and thus Device.getOperation() returns "None".
	 */
	protected void updateOpStatus(String stat) {
		String s = getOperationDescription() + ": " + stat;
		device.setOpStatus(s);
	}

	/**
	 * Query props, ensuring that sufficient time has passed since the
	 * transaction with the device (Cohu devices require a short delay
	 * between commands).
	 */
	protected void doQueryProps(CommMessage mess) throws IOException {
		pauseIfNeeded();
		mess.queryProps();
		poller.setLastCmdTime(System.currentTimeMillis());
	}

	/**
	 * Store props, ensuring that sufficient time has passed since the
	 * transaction with the device (Cohu devices require a short delay
	 * between commands).
	 */
	protected void doStoreProps(CommMessage<CohuPTZProperty> mess)
		throws IOException
	{
		pauseIfNeeded();
		mess.storeProps();
		poller.setLastCmdTime(System.currentTimeMillis());
	}

	/**
	 * If CohuPTZPoller.MIN_CMD_INTERVAL_MS milliseconds have not passed
	 * since the previous device transaction, sleep until they have.
	 */
	private void pauseIfNeeded() {
		long lastCmdTime = poller.getLastCmdTime();
		long curTime = System.currentTimeMillis();
		long delta = curTime - lastCmdTime;
		if (delta < MIN_CMD_INTERVAL_MS)
			TimeSteward.sleep_well(MIN_CMD_INTERVAL_MS - delta);
	}
}
