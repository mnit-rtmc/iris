/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2025  Minnesota Department of Transportation
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
	public OpControlGate(GateArmImpl g, GateArmState gas) {
		super(PriorityLevel.COMMAND, g);
		req_state = gas;
		control = new ControlProperty(password());
		control.setOpen(gas == GateArmState.OPENING);
		control.setClose(gas == GateArmState.CLOSING);
		control.setInterlock(g.isOpenDenied());
	}

	/** Create a new gate arm control operation (to set interlock only) */
	public OpControlGate(GateArmImpl g) {
		this(g, null);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<STCProperty> phaseTwo() {
		return new StoreControl();
	}

	/** Phase to store control */
	protected class StoreControl extends Phase<STCProperty> {

		/** Store control */
		protected Phase<STCProperty> poll(CommMessage<STCProperty> mess)
			throws IOException
		{
			mess.add(control);
			mess.storeProps();
			if (req_state != null)
				gate_arm.setArmStateNotify(req_state);
			return null;
		}
	}
}
