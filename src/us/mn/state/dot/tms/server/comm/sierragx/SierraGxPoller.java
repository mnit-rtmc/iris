/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
 * Copyright (C) 2018-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.sierragx;

import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.GpsImpl;
import us.mn.state.dot.tms.server.comm.GpsPoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import us.mn.state.dot.tms.utils.URIUtil;

/**
 * A Poller to communicate with Sierra Wireless GX4xx modems.
 *
 * @author John L. Stanley
 * @author Douglas Lau
 */
public class SierraGxPoller extends ThreadedPoller<SierraGxProperty>
	implements GpsPoller
{
	/** Create a new RedLion GPS poller */
	public SierraGxPoller(CommLink link) {
		super(link, URIUtil.TCP, GpsImpl.GPS_LOG);
	}

	/** Send a request to the GPS */
	@Override
	public void sendRequest(GpsImpl gps, DeviceRequest r) {
		switch (r) {
		case QUERY_STATUS:
			addOp(new OpQueryGpsLocation(gps, false));
			break;
		case QUERY_GPS_LOCATION:
			addOp(new OpQueryGpsLocation(gps, true));
			break;
		default:
			// Ignore other requests
			break;
		}
	}
}
