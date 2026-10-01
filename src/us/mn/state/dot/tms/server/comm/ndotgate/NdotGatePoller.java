/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2026  SRF Consulting Group
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

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.GateArmPoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * A Poller to communicate with Nebraska and North Dakota
 *  gate controllers using the NDOTv6 gate control protocol.
 * (This code is backwards compatible with the NDORv5 gate
 *  control protocol.)
 *
 * Note1:  Updated in August 2016 to include multi-arm
 *  gate protocol referred to as NDORv5.
 * Note2:  Updated in January 2026 to include the NDOTv6
 *  MODBUS error reporting extension for North Dakota.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class NdotGatePoller extends ThreadedPoller<NdotGateProperty>
	implements GateArmPoller
{
	/** Debug log */
	static protected final DebugLog NDOTGATE_LOG =
			new DebugLog("ndotgate");

	/** Create a new NDOT Gate poller */
	public NdotGatePoller(CommLink link) {
		super(link, TCP, NDOTGATE_LOG);
	}

	/** Send a device request */
	@SuppressWarnings("unchecked")
	// Note:  The open-gate/close-gate operations are handled
	// separately by the openGate(...) and closeGate(...) methods
	// because they require info about the user requesting the op.
	@Override
	public void sendRequest(GateArmImpl ga, DeviceRequest r) {
		switch (r) {
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
	public void openGate(GateArmImpl ga) {
		addOp(new OpMoveGateArm(ga, GateArmState.OPENING));
	}

	/** Close the gate arm */
	@SuppressWarnings("unchecked")
	@Override
	public void closeGate(GateArmImpl ga) {
		addOp(new OpMoveGateArm(ga, GateArmState.CLOSING));
	}
}
