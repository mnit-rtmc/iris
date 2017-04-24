/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2017  Minnesota Department of Transportation
 * Copyright (C) 2014  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.manchester;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CameraPoller;
import us.mn.state.dot.tms.server.comm.TransientPoller;
import static us.mn.state.dot.tms.utils.URIUtil.UDP;

/**
 * ManchesterPoller is a java implementation of the Manchester (American
 * Dynamics) camera control communication protocol
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class ManchesterPoller extends TransientPoller<ManchesterProperty>
	implements CameraPoller
{
	/** Manchester debug log */
	static private final DebugLog MANCHESTER_LOG =
		new DebugLog("manchester");

	/** Create a new Manchester poller */
	public ManchesterPoller(String n) {
		super(n, UDP, MANCHESTER_LOG);
	}

	/** Send a PTZ camera move command */
	@Override
	public void sendPTZ(CameraImpl c, float p, float t, float z) {
		addOp(new OpMoveCamera(c, p, t, z));
	}

	/** Send a store camera preset command */
	@Override
	public void sendStorePreset(CameraImpl c, int preset) {
		addOp(new OpPreset(c, true, preset));
	}

	/** Send a recall camera preset command */
	@Override
	public void sendRecallPreset(CameraImpl c, int preset) {
		addOp(new OpPreset(c, false, preset));
	}

	/** Send a device request
	 * @param c The CameraImpl object.
	 * @param dr The desired DeviceRequest. */
	@Override
	public void sendRequest(CameraImpl c, DeviceRequest dr) {
		if (DeviceRequest.QUERY_STATUS != dr)
			addOp(new OpDeviceRequest(c, dr));
	}
}
