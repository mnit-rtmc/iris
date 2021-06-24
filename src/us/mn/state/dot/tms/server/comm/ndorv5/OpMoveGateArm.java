/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2021  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.ndorv5;

import java.io.IOException;

import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.FutureOp;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to control gate arm.
 *
 * @author John L. Stanley - SRF Consulting
 */
@SuppressWarnings("rawtypes")
public class OpMoveGateArm extends OpGateNdorV5 {

	/** Requested gate arm state */
	private GateArmState target_state = GateArmState.UNKNOWN;
	
	/** Create a new gate arm control operation */
	@SuppressWarnings("unchecked")
	public OpMoveGateArm(GateArmImpl d, User o, GateArmState gas) {
		super(PriorityLevel.COMMAND, d, false); // priority 1, non-exclusive
		user = o;
		if (sGateArm == null) {
			prop = null;
			return;
		}
		if (gas == GateArmState.OPENING) {
			// NDORv5 "Raise Gate" command
			prop = new GateNdorV5Property("*R"+sGateArm+"#\r\n");
			target_state = GateArmState.OPEN;
		}
		else if ((gas == GateArmState.BEACON_ON)
		      || (gas == GateArmState.CLOSING)) {
			// NDORv5 "Lower Gate" command
			prop = new GateNdorV5Property("*L"+sGateArm+"#\r\n");
			target_state = GateArmState.CLOSED;
		}
		else {
			prop = null;
		}
	}

	/** Create a new gate arm control operation (to set interlock only) */
	public OpMoveGateArm(GateArmImpl d) {
		this(d, null, null);
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return (prop == null)
		     ? null
		     : (new StartGateArmMotion());
	}

	/** Phase to start gate-arm motion */
	protected class StartGateArmMotion extends Phase {

		int failCount = 0;

		/** Store control */
		@SuppressWarnings({ "unchecked", "synthetic-access", "incomplete-switch" })
		protected Phase poll(CommMessage mess)
			throws IOException
		{
			mess.add(prop);
			mess.storeProps();

			if (prop.gotValidResponse() == false) {
				if (failCount++ < 3) {
					return this; // try again
				}

				handleCommError(EventType.COMM_FAILED, "No Response");
				updateStatus();
				return null;
			}
			
			// queue a lower priority op to monitor the operation
			OpDevice op;
			switch (target_state) {
				case CLOSED:
					int delay = prop.delay;
					op = new OpCheckMoveStatus((GateArmImpl)device, user, target_state, delay);
					gate_arm.setBeaconOn(true);
					FutureOp.queueOp(device, delay, op);
					setNdorV5ArmStateNotify(GateArmState.BEACON_ON, user);
					break;
				case OPEN:
					op = new OpCheckMoveStatus((GateArmImpl)device, user, target_state, 0);
					FutureOp.queueOp(device, 2, op);
					setNdorV5ArmStateNotify(GateArmState.OPENING, user);
					break;
			}
			updateStatus();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		super.cleanup();
		if(!isSuccess())
			gate_arm.checkTimeout();
	}

}
