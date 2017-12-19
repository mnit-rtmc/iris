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
package us.mn.state.dot.tms.server.comm.monstream;

import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.VideoMonitorImpl;
import us.mn.state.dot.tms.server.comm.BasePoller;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;
import us.mn.state.dot.tms.server.comm.VideoMonitorPoller;
import static us.mn.state.dot.tms.server.comm.PriorityLevel.COMMAND;
import static us.mn.state.dot.tms.utils.URIUtil.UDP;

/**
 * MonStreamPoller impelemnts the MonStream video monitor switching protocol.
 *
 * @author Douglas Lau
 */
public class MonStreamPoller extends BasePoller implements VideoMonitorPoller {

	/** Create a new MonStream poller */
	public MonStreamPoller(String n) {
		super(n, UDP);
	}

	/** Create an operation */
	private void createOp(String n, VideoMonitorImpl vm, OpStep s) {
		Operation op = new Operation(n, vm, s);
		op.setPriority(COMMAND);
		addOp(op);
	}

	/** Set the camera to display on the specified monitor */
	@Override
	public void switchCamera(VideoMonitorImpl vm, CameraImpl cam) {
		createOp("video.monitor.op.switch", vm,new OpSwitchCamera(cam));
	}

	/** Send a device request
	 * @param vm The VideoMonitor object.
	 * @param dr The desired DeviceRequest. */
	@Override
	public void sendRequest(VideoMonitorImpl vm, DeviceRequest dr) {
		switch (dr) {
		case SEND_SETTINGS:
			createOp("video.monitor.op.config", vm,
				new OpMonitor());
			break;
		case QUERY_STATUS:
			addOp(new Operation("video.monitor.op.query",
				(ControllerImpl) vm.getController(),
				new OpStatus()));
			break;
		default:
			// Ignore other requests
			break;
		}
	}
}
