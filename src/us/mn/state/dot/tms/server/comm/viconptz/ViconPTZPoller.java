/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.viconptz;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.BasePoller;
import us.mn.state.dot.tms.server.comm.CameraPoller;
import us.mn.state.dot.tms.server.comm.Operation;
import static us.mn.state.dot.tms.utils.URIUtil.UDP;

/**
 * ViconPoller is a java implementation of the Vicon camera control
 * communication protocol.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class ViconPTZPoller extends BasePoller implements CameraPoller {

	/** Vicon PTZ debug log */
	static private final DebugLog VICON_LOG = new DebugLog("viconptz");

	/** Create a new Vicon poller */
	public ViconPTZPoller(String n) {
		super(n, UDP, VICON_LOG);
	}

	/** Send a PTZ camera move command */
	@Override
	public void sendPTZ(CameraImpl c, float p, float t, float z) {
		addOp(new Operation("camera.op.send.ptz", c,
			new OpMoveCamera(p, t, z)));
	}

	/** Send a store camera preset command */
	@Override
	public void sendStorePreset(CameraImpl c, int preset) {
		addOp(new Operation("camera.op.store.preset", c,
			new OpPreset(true, preset)));
	}

	/** Send a recall camera preset command */
	@Override
	public void sendRecallPreset(CameraImpl c, int preset) {
		addOp(new Operation("camera.op.recall.preset", c,
			new OpPreset(false, preset)));
	}

	/** Send a device request
	 * @param c The CameraImpl object.
	 * @param dr The desired DeviceRequest. */
	@Override
	public void sendRequest(CameraImpl c, DeviceRequest dr) {
		addOp(new Operation("device.op.request", c,
			new OpDeviceRequest(dr)));
	}
}
