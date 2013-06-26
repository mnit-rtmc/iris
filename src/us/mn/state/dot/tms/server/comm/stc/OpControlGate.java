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
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to control gate arm.
 *
 * @author Douglas Lau
 */
public class OpControlGate extends OpSTC {

	/** Control property */
	private final ControlProperty control;

	/** Requested gate arm state */
	private final GateArmState req_state;

	/** Create a new gate arm control operation */
	public OpControlGate(GateArmImpl d, GateArmState gas) {
		super(PriorityLevel.COMMAND, d);
		control = new ControlProperty();
		control.setOpen(gas == GateArmState.OPENING);
		control.setClose(gas == GateArmState.CLOSING);
		req_state = gas;
	}

	/** Create the second phase of the operation */
	protected Phase<STCProperty> phaseTwo() {
		return new StoreControl();
	}

	/** Phase to store control */
	protected class StoreControl extends Phase<STCProperty> {

		/** Store control */
		protected Phase<STCProperty> poll(CommMessage mess)
			throws IOException
		{
			mess.add(control);
			logStore(control);
			mess.storeProps();
			// Verify requested status is needed so that
			// OpQueryGateStatus can't sneak in and revert
			// the gate arm to state it was in pre-request
			return new VerifyStatus();
		}
	}

	/** Phase to verify the requested gate status */
	protected class VerifyStatus extends Phase<STCProperty> {

		/** Number of tries to verify status */
		private int n_tries = 0;

		/** Verify the requested status */
		protected Phase<STCProperty> poll(CommMessage mess)
			throws IOException
		{
			StatusProperty s = new StatusProperty();
			mess.add(s);
			mess.queryProps();
			logQuery(s);
			setMaintStatus(s.getMaintStatus());
			// Try up to 3 times to verify that the requested
			// state has been accepted before giving up
			if(s.getState() != req_state) {
				n_tries++;
				if(n_tries < 3)
					return this;
			}
			gate_arm.setArmStateNotify(s.getState());
			return null;
		}
	}
}
