/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.org815;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.WeatherPoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * Org815Poller is a weather poller for the Optical Scientific ORG-815 sensor.
 *
 * @author Douglas Lau
 */
public class Org815Poller extends DevicePoller<Org815Property>
	implements WeatherPoller
{
	/** ORG-815 debug log */
	static private final DebugLog ORG815_LOG = new DebugLog("org815");

	/** Create a new ORG-815 poller */
	public Org815Poller(String n) {
		super(n, TCP, ORG815_LOG);
	}

	/** Send a device request */
	@Override
	public void sendRequest(WeatherSensorImpl ws, DeviceRequest r) {
		switch (r) {
		case QUERY_STATUS:
			addOp(new OpQueryConditions(ws));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send settings to a weather sensor */
	@Override
	public void sendSettings(WeatherSensorImpl ws) {
		addOp(new OpQuerySettings(ws));
	}
}
