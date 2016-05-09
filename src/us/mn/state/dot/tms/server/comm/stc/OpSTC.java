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

import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation for STC device
 *
 * @author Douglas Lau
 */
abstract public class OpSTC extends OpDevice<STCProperty> {

	/** Gate arm device */
	protected final GateArmImpl gate_arm;

	/** Create a new STC operation */
	protected OpSTC(PriorityLevel p, GateArmImpl ga, boolean ex) {
		super(p, ga, ex);
		gate_arm = ga;
	}

	/** Create a new STC operation */
	protected OpSTC(PriorityLevel p, GateArmImpl ga) {
		super(p, ga);
		gate_arm = ga;
	}

	/** Get the controller password */
	public String password() {
		return getController().getPassword();
	}
}
