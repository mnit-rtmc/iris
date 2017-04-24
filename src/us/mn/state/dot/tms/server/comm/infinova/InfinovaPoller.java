/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.infinova;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CameraPoller;
import us.mn.state.dot.tms.server.comm.MessengerException;
import us.mn.state.dot.tms.server.comm.TransientPoller;
import us.mn.state.dot.tms.server.comm.pelcod.OpDeviceRequest;
import us.mn.state.dot.tms.server.comm.pelcod.OpMoveCamera;
import us.mn.state.dot.tms.server.comm.pelcod.OpPreset;
import us.mn.state.dot.tms.server.comm.pelcod.PelcoDProperty;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * Infinova poller
 *
 * @author Douglas Lau
 */
public class InfinovaPoller extends TransientPoller<PelcoDProperty>
	implements CameraPoller
{
	/** Infinova debug log */
	static public final DebugLog INF_LOG = new DebugLog("infinova");

	/** Create a new infinova poller */
	public InfinovaPoller(String n) {
		super(n, TCP, INF_LOG);
	}

	/** Create a comm thread */
	@Override
	protected InfinovaThread createCommThread(String uri, int timeout) {
		return new InfinovaThread(this, queue, scheme, uri, timeout,
			INF_LOG);
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
