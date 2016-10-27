/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.axisptz;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CameraPoller;
import us.mn.state.dot.tms.server.comm.TransientPoller;
import static us.mn.state.dot.tms.utils.URIUtil.HTTP;

/**
 * Axis VAPIX PTZ poller.
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class AxisPTZPoller extends TransientPoller<AxisProp>
	implements CameraPoller
{
	/** Debug log */
	static private final DebugLog AXIS_LOG = new DebugLog("axisptz");

	/** Create a device request property */
	static private AxisProp createDeviceReqProp(DeviceRequest r) {
		PTZCommandProp prop = new PTZCommandProp();
		switch (r) {
		case CAMERA_FOCUS_NEAR:
			prop.addFocus(-1);
			return prop;
		case CAMERA_FOCUS_FAR:
			prop.addFocus(1);
			return prop;
		case CAMERA_FOCUS_STOP:
			prop.addFocus(0);
			return prop;
		case CAMERA_IRIS_CLOSE:
			prop.addIris(-1);
			return prop;
		case CAMERA_IRIS_OPEN:
			prop.addIris(1);
			return prop;
		case CAMERA_IRIS_STOP:
			prop.addIris(0);
			return prop;
		case CAMERA_FOCUS_MANUAL:
			prop.addAutoFocus(false);
			return prop;
		case CAMERA_FOCUS_AUTO:
			prop.addAutoFocus(true);
			return prop;
		case CAMERA_IRIS_MANUAL:
			prop.addAutoIris(false);
			return prop;
		case CAMERA_IRIS_AUTO:
			prop.addAutoIris(true);
			return prop;
		case RESET_DEVICE:
		case CAMERA_WIPER_ONESHOT:
			// FIXME: create SerialWriteProp
			return null;
		default:
			return null;
		}
	}

	/** Create a new Axis PTZ poller.
	 *
	 * @param n CommLink name.
	 */
	public AxisPTZPoller(String n) {
		super(n, HTTP, AXIS_LOG);
	}

	/** Send a PTZ camera move command */
	@Override
	public void sendPTZ(CameraImpl c, float p, float t, float z) {
		PTZCommandProp prop = new PTZCommandProp();
		prop.addPanTilt(p, t);
		prop.addZoom(z);
		addOp(new OpAxisPTZ(c, prop));
	}

	/** Send a store camera preset command */
	@Override
	public void sendStorePreset(CameraImpl c, int preset) {
		PTZConfigProp prop = new PTZConfigProp();
		prop.addStorePreset(preset);
		addOp(new OpAxisPTZ(c, prop));
	}

	/** Send a recall camera preset command */
	@Override
	public void sendRecallPreset(CameraImpl c, int preset) {
		PTZCommandProp prop = new PTZCommandProp();
		prop.addRecallPreset(preset);
		addOp(new OpAxisPTZ(c, prop));
	}

	/** Send a device request.
	 *
	 * @param c The CameraImpl object.
	 * @param r The desired DeviceRequest.
	 */
	@Override
	public void sendRequest(CameraImpl c, DeviceRequest r) {
		AxisProp prop = createDeviceReqProp(r);
		if (prop != null)
			addOp(new OpAxisPTZ(c, prop));
	}
}
