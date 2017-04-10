/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelco;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.VideoMonitorImpl;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import us.mn.state.dot.tms.server.comm.VideoMonitorPoller;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * PecloPoller is a java implementation of the Pelco Video Switch serial
 * communication protocol
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
public class PelcoPoller extends ThreadedPoller<PelcoProperty>
	implements VideoMonitorPoller
{
	/** Pelco debug log */
	static protected final DebugLog PELCO_LOG = new DebugLog("pelco");

	/** Maximum monitor number allowed by Pelco switch */
	static private final int MAX_MONITOR_NUM = 255;

	/** Create a new Pelco line */
	public PelcoPoller(String n) {
		super(n, TCP, PELCO_LOG);
	}

	/** Set the camera to display on the specified monitor */
	@Override
	public void switchCamera(ControllerImpl c, VideoMonitorImpl vm,
		CameraImpl cam)
	{
		int mn = vm.getMonNum();
		if (mn > 0 && mn <= MAX_MONITOR_NUM)
			addOp(new OpSelectMonitorCamera(c, vm, cam));
		else if (PELCO_LOG.isOpen())
			PELCO_LOG.log("Invalid monitor: " + vm.getMonNum());
	}

	/** Send a device request
	 * @param vm The VideoMonitor object.
	 * @param r The desired DeviceRequest. */
	@Override
	public void sendRequest(VideoMonitorImpl vm, DeviceRequest r) {
		// No supported requests
	}
}
