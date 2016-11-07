/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query gate arm status.
 *
 * @author Douglas Lau
 */
public class OpQueryGateStatus extends OpSTC {

	/** Interval to update controller operation count */
	static private final long OP_COUNT_INTERVAL_MS = 30 * 1000;

	/** Status property */
	private final StatusProperty status;

	/** Time to update operation counts */
	private long op_time = TimeSteward.currentTimeMillis();

	/** Create a new gate arm query status operation */
	public OpQueryGateStatus(GateArmImpl d) {
		// Don't require exclusive access to device, since this
		// operation loops continuously.  This prevents priority from
		// being changed due to DeviceContentionException.  Bumping
		// priority here can starve other operations (due to looping).
		super(PriorityLevel.DEVICE_DATA, d, false);
		status = new StatusProperty(password());
	}

	/** Create the second phase of the operation */
	protected Phase<STCProperty> phaseTwo() {
		return new QueryVersion();
	}

	/** Phase to query the version */
	protected class QueryVersion extends Phase<STCProperty> {

		/** Query the version */
		protected Phase<STCProperty> poll(CommMessage<STCProperty> mess)
			throws IOException
		{
			VersionProperty v = new VersionProperty(password());
			mess.add(v);
			mess.queryProps();
			gate_arm.setVersionNotify(v.getVersion());
			return new QueryStatus();
		}
	}

	/** Phase to query the gate status */
	protected class QueryStatus extends Phase<STCProperty> {

		/** Query the status */
		protected Phase<STCProperty> poll(CommMessage<STCProperty> mess)
			throws IOException
		{
			mess.add(status);
			mess.queryProps();
			updateStatus();
			return this;
		}
	}

	/** Update controller status */
	private void updateStatus() {
		gate_arm.setArmStateNotify(status.getState(), null);
		setMaintStatus(status.getMaintStatus());
		updateMaintStatus();
		if (shouldUpdateOpCount()) {
			controller.completeOperation(id, isSuccess());
			op_time += OP_COUNT_INTERVAL_MS;
		}
	}

	/** Check if we should update the controller operation count */
	private boolean shouldUpdateOpCount() {
		return TimeSteward.currentTimeMillis() >= op_time;
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		super.cleanup();
		if (!isSuccess())
			gate_arm.checkTimeout();
	}
}
