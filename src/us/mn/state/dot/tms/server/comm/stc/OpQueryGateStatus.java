/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.stc;

import java.io.IOException;
import us.mn.state.dot.sched.TimeSteward;
import static us.mn.state.dot.tms.GateArmState.TIMEOUT;
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query gate arm status.
 *
 * @author Douglas Lau
 */
public class OpQueryGateStatus extends OpSTC {

	/** Timeout (ms) for a comm failure to result in TIMEOUT status */
	static private final long FAIL_TIMEOUT_MS = 90 * 1000;

	/** Status property */
	private final StatusProperty status;

	/** Flag for controller status update */
	private boolean status_update = true;

	/** Create a new gate arm query status operation */
	public OpQueryGateStatus(GateArmImpl d) {
		super(PriorityLevel.DEVICE_DATA, d);
		status = new StatusProperty(password());
	}

	/** Create the second phase of the operation */
	protected Phase<STCProperty> phaseTwo() {
		return new QueryVersion();
	}

	/** Phase to query the version */
	protected class QueryVersion extends Phase<STCProperty> {

		/** Query the version */
		protected Phase<STCProperty> poll(CommMessage mess)
			throws IOException
		{
			VersionProperty v = new VersionProperty(password());
			mess.add(v);
			mess.queryProps();
			logQuery(v);
			gate_arm.setVersion(v.getVersion());
			// Don't hold device lock while looping
			device.release(operation);
			return new QueryStatus();
		}
	}

	/** Phase to query the gate status */
	protected class QueryStatus extends Phase<STCProperty> {

		/** Query the status */
		protected Phase<STCProperty> poll(CommMessage mess)
			throws IOException
		{
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			updateStatus();
			return this;
		}
	}

	/** Update controller status */
	private void updateStatus() {
		gate_arm.setArmStateNotify(status.getState(), null);
		setMaintStatus(status.getMaintStatus());
		updateMaintStatus();
		if(status_update)
			controller.completeOperation(id, isSuccess());
		status_update = false;
	}

	/** Cleanup the operation */
	@Override public void cleanup() {
		super.cleanup();
		if(!isSuccess() && isFailThresholdExpired())
			gate_arm.setArmStateNotify(TIMEOUT, null);
	}

	/** Test if comm has failed for longer than threshold */
	private boolean isFailThresholdExpired() {
		Long ft = controller.getFailTime();
		return ft != null && TimeSteward.currentTimeMillis() >
			ft + FAIL_TIMEOUT_MS;
	}
}
