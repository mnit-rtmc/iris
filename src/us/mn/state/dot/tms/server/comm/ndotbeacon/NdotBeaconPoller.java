/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2022  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.ndotbeacon;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.BeaconImpl;
import us.mn.state.dot.tms.server.comm.BeaconPoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * Poller to control NDOT (Nebraska DOT) beacon controllers.
 *
 * (Note:  The NDOT Beacon controller firmware is
 *  derived from the NDOT/NDORv5 Gate controller
 *  firmware, and uses a very similar protocol...)
 *
 * @author John L. Stanley - SRF Consulting
 */
public class NdotBeaconPoller extends ThreadedPoller<NdotBeaconProperty>
	implements BeaconPoller
{
	/** NDOT Beacon debug log */
	static final DebugLog NDOTBEACON_LOG =
			new DebugLog("ndotbeacon");

	/** Create a new NDOT Beacon poller */
	public NdotBeaconPoller(CommLink link) {
		super(link, TCP, NDOTBEACON_LOG);
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
