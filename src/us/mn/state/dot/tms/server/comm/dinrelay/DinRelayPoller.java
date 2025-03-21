/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dinrelay;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.User;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.LcsImpl;
import us.mn.state.dot.tms.server.comm.BeaconPoller;
import us.mn.state.dot.tms.server.comm.LCSPoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.HTTP;

/**
 * Poller to control Digital Loggers Inc DIN Relay devices.
 *
 * @author Douglas Lau
 */
public class DinRelayPoller extends ThreadedPoller<DinRelayProperty>
	implements LCSPoller, BeaconPoller
{
	/** DIN relay debug log */
	static final DebugLog DIN_LOG = new DebugLog("dinrelay");

	/** Create a new DIN relay poller */
	public DinRelayPoller(CommLink link) {
		super(link, HTTP, DIN_LOG);
	}

	/** Query the outlet status */
	public void queryOutlets(ControllerImpl c, OutletProperty op) {
		addOp(new OpQueryOutlets(c, op));
	}

	/** Command the outlet status */
	public void commandOutlets(ControllerImpl c, boolean[] outlets,
		OutletProperty op)
	{
		addOp(new OpCommandOutlets(c, outlets, op));
	}

	/** Send a device request */
	@Override
	public void sendRequest(LcsImpl lcs, DeviceRequest r) {
		switch (r) {
		case QUERY_MESSAGE:
			addOp(new OpQueryLCSIndications(lcs));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send new indications to an LCS array.
	 * @param lcs LCS array.
	 * @param lock LCS lock (JSON), or null. */
	@Override
	public void sendIndications(LcsImpl lcs, String lock) {
		addOp(new OpSendLCSIndications(lcs, lock));
	}

	/** Send a device request */
	@Override
	public void sendRequest(BeaconImpl b, DeviceRequest r) {
		switch (r) {
		case QUERY_STATUS:
			addOp(new OpQueryBeaconState(b));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Set the flashing state of a beacon */
	@Override
	public void setFlashing(BeaconImpl b, boolean f) {
		addOp(new OpChangeBeaconState(b, f));
		addOp(new OpQueryBeaconState(b));
	}
}
