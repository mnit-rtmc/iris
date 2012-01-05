/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.util.LinkedList;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to send settings to a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpSendLCSSettings extends OpLCS {

	/** List of operations to send settings to DMS in LCS array */
	protected final LinkedList<OpDMS> ops = new LinkedList<OpDMS>();

	/** Create a new operation to send LCS settings */
	public OpSendLCSSettings(LCSArrayImpl l) {
		super(PriorityLevel.DEVICE_DATA, l);
		createOperations();
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		if(ops.isEmpty())
			return null;
		else
			return new StartOperations();
	}

	/** Phase to start DMS operations */
	protected class StartOperations extends Phase {

		/** Start all DMS operations */
		protected Phase poll(CommMessage mess) {
			for(OpDMS op: ops)
				op.start();
			return new WaitForCompletion();
		}
	}

	/** Phase to wait for operations to complete */
	protected class WaitForCompletion extends Phase {

		/** Time to stop waiting for completion (20 seconds) */
		protected final long expire = TimeSteward.currentTimeMillis() + 
			20 * 1000;

		/** Wait for operations to complete */
		protected Phase poll(CommMessage mess) {
			try {
				Thread.sleep(200);
			}
			catch(InterruptedException e) {
				// Ignore
			}
			OpDMS op = ops.peekFirst();
			if(op == null)
				return null;
			if(op.isDone())
				ops.removeFirst();
			else {
				if(TimeSteward.currentTimeMillis() > expire) {
					LCS_LOG.log(lcs_array.getName() +
						": LCS timeout expired -- " +
						"giving up");
					success = false;
					return null;
				}
			}
			return this;
		}
	}

	/** Create operations to send new indications to an LCS array */
	protected void createOperations() {
		for(DMSImpl dms: dmss) {
			if(dms != null)
				ops.add(new OpSendDMSGraphics(dms));
		}
	}
}
