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

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.GateArmPoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * A Poller to communicate with an NDOR (Nebraska Department
 * of Roads) Gate Control Controller using their v5 protocol.
 * Note:  Code updated in August 2016 to include multi-arm
 * gate protocol referred to as v5.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class GateNdorV5Poller
  extends ThreadedPoller<GateNdorV5Property>
  implements GateArmPoller
{
	/** Debug log */
	static protected final DebugLog GATENDORv5_LOG =
			new DebugLog("gatendorv5");

	/** Create a new NDORv5 Gate poller */
	public GateNdorV5Poller(CommLink link) {
		super(link, TCP, GATENDORv5_LOG);
	}

	/** Send a device request */
	@SuppressWarnings("unchecked")
	// Note:  The open-gate/close-gate operations are handled
	// separately by the openGate(...) and closeGate(...) methods
	// because they require info about the user requesting the op.
	@Override
	public void sendRequest(GateArmImpl ga, DeviceRequest r) {
		switch (r) {
//			case SEND_SETTINGS:
// Send-settings operation is not available for NDOR Gates.
//				break;
//			case RESET_DEVICE:
// Reset-device operation is not available for NDOR Gates.
//				break;
			case QUERY_STATUS:
				addOp(new OpQueryGateStatus(ga));
				break;
			default:
				// Ignore other requests
				break;
		}
	}

	/** Open the gate arm */
	@SuppressWarnings("unchecked")
	@Override
	public void openGate(GateArmImpl ga, User o) {
		addOp(new OpMoveGateArm(ga, o, GateArmState.OPENING));
	}

	/** Close the gate arm */
	@SuppressWarnings("unchecked")
	@Override
	public void closeGate(GateArmImpl ga, User o) {
		// On NDOR gate controllers, the gate first goes
		// into a beacon-on state, and then automatically
		// goes into the closing state.
		addOp(new OpMoveGateArm(ga, o, GateArmState.BEACON_ON));
	}
}
