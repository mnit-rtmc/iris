/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
 * Copyright (C) 2016-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.onvifptz;

import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CameraPoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;
import static us.mn.state.dot.tms.utils.URIUtil.HTTP;

/**
 * Poller for the Onvif PTZ protocol.
 *
 * @author Douglas Lau
 * @author Ethan Beauclaire
 */
public class OnvifPTZPoller extends ThreadedPoller<OnvifProp> implements CameraPoller {

	static private final DebugLog ONVIF_LOG = new DebugLog("onvifptz");

	/** Write a message to the protocol log */
	static protected void slog(String msg) {
		if (ONVIF_LOG.isOpen())
			ONVIF_LOG.log(msg);
	}

	/** Create a new Onvif PTZ poller */
	public OnvifPTZPoller(CommLink link) {
		super(link, HTTP, ONVIF_LOG);
	}

	static private PTZCommandProp createDeviceReqProp(PTZCommandProp prop, DeviceRequest r) {
		if (prop == null) return null;

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
		// 0dB is completely open, >0 is more closed
		case CAMERA_IRIS_CLOSE:
			prop.addIris(1);
			return prop;
		case CAMERA_IRIS_OPEN:
			prop.addIris(-1);
			return prop;
		// unnecessary, only absolute iris command in ONVIF:
		//case CAMERA_IRIS_STOP:
		//	prop.addIris(0);
		//	return prop;
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
		case CAMERA_WIPER_ONESHOT:
			prop.addWiperOneshot();
			return prop;
		case RESET_DEVICE:
			// TODO: determine meaning
			// for now, set focus/iris to auto
			prop.addAutoIrisAndFocus();
			return prop;
		default:
			return null;
		}
	}

	/** Send a PTZ camera move command */
	@Override
	public void sendPTZ(CameraImpl c, float p, float t, float z) {
		PTZCommandProp prop = new PTZCommandProp("ptz");
		String url = c.getController().getCommLink().getUri();
		prop.setUrl(url);
		prop.addPanTiltZoom(p, t, z);
		addOp(new OpOnvifPTZ(c, prop));
	}

	/** Send a "store camera preset" command */
	@Override
	public void sendStorePreset(CameraImpl c, int preset) {
		PTZCommandProp prop = new PTZCommandProp("ptz");
		String url = c.getController().getCommLink().getUri();
		prop.setUrl(url);
		prop.addStorePreset(preset);
		addOp(new OpOnvifPTZ(c, prop));
	}

	/** Send a "recall camera preset" command */
	@Override
	public void sendRecallPreset(CameraImpl c, int preset) {
		PTZCommandProp prop = new PTZCommandProp("ptz");
		String url = c.getController().getCommLink().getUri();
		prop.setUrl(url);
		prop.addRecallPreset(preset);
		addOp(new OpOnvifPTZ(c, prop));
	}

	/** Send a device request.
	 * @param c The CameraImpl object.
	 * @param dr Device request to send. */
	@Override
	public void sendRequest(CameraImpl c, DeviceRequest dr) {
		PTZCommandProp prop = new PTZCommandProp("imaging");
		if (dr == DeviceRequest.CAMERA_WIPER_ONESHOT)
			prop = new PTZCommandProp("ptz");

		String url = c.getController().getCommLink().getUri();
		prop.setUrl(url);
		prop = createDeviceReqProp(prop, dr);
		if (prop != null)
			addOp(new OpOnvifPTZ(c, prop));
	}
}
