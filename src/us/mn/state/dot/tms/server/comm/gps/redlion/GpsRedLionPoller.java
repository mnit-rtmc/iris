/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.gps.redlion;

import java.net.URI;

import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.GpsImpl;
import us.mn.state.dot.tms.server.comm.GpsPoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import us.mn.state.dot.tms.server.comm.gps.GpsProperty;
import us.mn.state.dot.tms.utils.URIUtil;

/**
 * A Poller to communicate with a GPS using
 *  RedLion AT+BMDIAG command.
 *
 * @author John L. Stanley
 */
public class GpsRedLionPoller
		extends ThreadedPoller<GpsProperty>
		implements GpsPoller {

	/** Create a new RedLion GPS poller */
	public GpsRedLionPoller(String n) {
		super(n, URIUtil.TCP, GpsImpl.GPS_LOG);
		attrCommIdleDisconnect = 
				SystemAttrEnum.COMM_IDLE_DISCONNECT_GPS_SEC;
	}

	/** Send a request to the GPS */
	@Override
	public void sendRequest(GpsImpl gps, DeviceRequest r) {
		switch (r) {
			case QUERY_STATUS:
			case QUERY_GPS_LOCATION:
				addOp(new OpQueryGpsLocationRedLion(gps, false));
				break;
			case QUERY_GPS_LOCATION_FORCE:
				addOp(new OpQueryGpsLocationRedLion(gps, true));
				break;
			default:
				; // Ignore other requests
		}
	}
}
