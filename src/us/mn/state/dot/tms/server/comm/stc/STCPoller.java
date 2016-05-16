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

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.CommThread;
import us.mn.state.dot.tms.server.comm.GateArmPoller;
import us.mn.state.dot.tms.server.comm.Messenger;

/**
 * STCPoller is a java implementation of the Hysecurity STC protocol.
 *
 * @author Douglas Lau
 */
public class STCPoller extends CommThread<STCProperty>
	implements GateArmPoller
{
	/** Debug log */
	static private final DebugLog STC_LOG = new DebugLog("stc");

	/** Create a new STC poller */
	public STCPoller(String n, Messenger m) {
		super(n, m, STC_LOG);
	}

	/** Send a device request */
	@Override
	public void sendRequest(GateArmImpl ga, DeviceRequest r) {
		switch(r) {
		case SEND_SETTINGS:
			addOp(new OpControlGate(ga));
			break;
		case RESET_DEVICE:
			addOp(new OpResetGate(ga));
			break;
		case QUERY_STATUS:
			addOp(new OpQueryGateStatus(ga));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Open the gate arm */
	@Override
	public void openGate(GateArmImpl ga, User o) {
		addOp(new OpControlGate(ga, o, GateArmState.OPENING));
	}

	/** Close the gate arm */
	@Override
	public void closeGate(GateArmImpl ga, User o) {
		addOp(new OpControlGate(ga, o, GateArmState.CLOSING));
	}
}
