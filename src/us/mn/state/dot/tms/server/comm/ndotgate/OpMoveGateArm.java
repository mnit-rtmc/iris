/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2026  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.ndotgate;

import java.io.IOException;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to control gate arm.
 *
 * @author John L. Stanley - SRF Consulting
 * @author Douglas Lau
 */
@SuppressWarnings("rawtypes")
public class OpMoveGateArm extends OpNdotGate {

	/** Requested gate arm state */
	private GateArmState target_state = GateArmState.UNKNOWN;

	/** Create a new gate arm control operation */
	@SuppressWarnings("unchecked")
	public OpMoveGateArm(GateArmImpl d, GateArmState gas) {
		super(PriorityLevel.COMMAND, d, false); // priority 1, non-exclusive
		if (sGateArm == null) {
			prop = null;
			return;
		}
		if (gas == GateArmState.OPENING) {
			// NDORv5/NDOTv6 "Raise Gate" command
			prop = new NdotGateProperty("*R"+sGateArm+"#\r\n");
			target_state = GateArmState.OPEN;
		}
		else if (gas == GateArmState.CLOSING) {
			// NDORv5/NDOTv6 "Lower Gate" command
			prop = new NdotGateProperty("*L"+sGateArm+"#\r\n");
			target_state = GateArmState.CLOSED;
		}
		else {
			prop = null;
		}
	}

	/** Create a new gate arm control operation (to set interlock only) */
	public OpMoveGateArm(GateArmImpl d) {
		this(d, null);
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return (prop == null)
		     ? null
		     : (new StartGateArmMotion());
	}

	/** Phase to start gate-arm motion */
	protected class StartGateArmMotion extends Phase {

		/** Store control */
		@SuppressWarnings({ "unchecked", "synthetic-access", "incomplete-switch" })
		protected Phase poll(CommMessage mess)
			throws IOException
		{
			mess.add(prop);
			mess.storeProps();
			if (!prop.gotValidResponse())
				throw new ParsingException("NO RESPONSE");

			// queue a lower priority op to monitor gate motion
			switch (target_state) {
				case CLOSED:
					checkClosing();
					break;
				case OPEN:
					checkOpening();
					break;
			}
			return null;
		}
	}

	/** Start (or retarget) op to check closing state */
	private void checkClosing() {
		OpCheckMoveStatus.monitor(gate_arm, target_state, prop.delay);
		// During a gate override, the controller ignores the close
		if (!prop.isGateOverride())
			gate_arm.setArmStateNotify(GateArmState.CLOSING);
	}

	/** Start (or retarget) op to check opening state */
	private void checkOpening() {
		OpCheckMoveStatus.monitor(gate_arm, target_state, 0);
		// During a gate override, the arm opens when it completes
		if (!prop.isGateOverride())
			gate_arm.setArmStateNotify(GateArmState.OPENING);
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		super.cleanup();
		if (!isSuccess())
			gate_arm.checkTimeout();
	}
}
