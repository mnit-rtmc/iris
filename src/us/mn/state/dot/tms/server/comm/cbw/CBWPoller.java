/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cbw;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.BeaconPoller;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import static us.mn.state.dot.tms.utils.URIUtil.HTTP;

/**
 * Poller to control Control By Web Relay devices.
 *
 * @author Douglas Lau
 */
public class CBWPoller extends DevicePoller<CBWProperty>
	implements BeaconPoller
{
	/** Control-By-Web debug log */
	static final DebugLog CBW_LOG = new DebugLog("cbw");

	/** Create a new CBW relay poller */
	public CBWPoller(String n) {
		super(n, HTTP, CBW_LOG);
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
	}
}
