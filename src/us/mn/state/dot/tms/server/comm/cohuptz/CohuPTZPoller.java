/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.cohuptz;

import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.comm.CameraPoller;
import us.mn.state.dot.tms.server.comm.Messenger;
import us.mn.state.dot.tms.server.comm.TransientPoller;

/**
 * Poller for the Cohu PTZ protocol
 *
 * @author Travis Swanston
 */
public class CohuPTZPoller extends TransientPoller<CohuPTZProperty>
	implements CameraPoller
{
	/** Timestamp of most recent transaction with the device. */
	private long lastCmdTime = 0;

	/** Current pan value */
	private float curPan  = 0.0F;

	/** Current tilt value */
	private float curTilt = 0.0F;

	/** Current zoom value */
	private float curZoom = 0.0F;

	/** Create a new Cohu PTZ poller */
	public CohuPTZPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Send a "PTZ camera move" command */
	@Override
	public void sendPTZ(CameraImpl c, float p, float t, float z) {
		Float pan  = null;
		Float tilt = null;
		Float zoom = null;

		if (p != curPan) {
			pan = Float.valueOf(p);
			curPan = p;
		}
		if (t != curTilt) {
			tilt = Float.valueOf(t);
			curTilt = t;
		}
		if (z != curZoom) {
			zoom = Float.valueOf(z);
			curZoom = z;
		}
		addOp(new OpPTZCamera(c, this, pan, tilt, zoom));
	}

	/** Send a "store camera preset" command */
	@Override
	public void sendStorePreset(CameraImpl c, int preset) {
		addOp(new OpStorePreset(c, this, preset));
	}

	/** Send a "recall camera preset" command */
	@Override
	public void sendRecallPreset(CameraImpl c, int preset) {
		addOp(new OpRecallPreset(c, this, preset));
	}

	/**
	 * Get the timestamp of the last command issued to the device.
	 * This value, stored in CohuPTZPoller, is updated by OpCohuPTZ
	 * operations via the CohuPTZPoller.setLastCmdTime method.
	 * @return The timestamp of the last command issued to the device,
	 *         or 0 if no commands have yet been issued.
	 */
	protected long getLastCmdTime() {
		return lastCmdTime;
	}

	/**
	 * Set the timestamp of the last command issued to the device.
	 * This value, stored in CohuPTZPoller, is updated by OpCohuPTZ
	 * operations.
	 * @param time The desired timestamp value to set.
	 */
	protected void setLastCmdTime(long time) {
		lastCmdTime = time;
	}

	/** Send a device request
	 * @param c The CameraImpl object.
	 * @param r The desired DeviceRequest. */
	@Override
	public void sendRequest(CameraImpl c, DeviceRequest r) {
		switch (r) {
		case CAMERA_FOCUS_STOP:
		case CAMERA_FOCUS_NEAR:
		case CAMERA_FOCUS_FAR:
			addOp(new OpMoveFocus(c, this, r));
			break;
		case CAMERA_FOCUS_MANUAL:
		case CAMERA_FOCUS_AUTO:
			addOp(new OpSetAFMode(c, this, r));
			break;
		case CAMERA_IRIS_STOP:
		case CAMERA_IRIS_CLOSE:
		case CAMERA_IRIS_OPEN:
			addOp(new OpMoveIris(c, this, r));
			break;
		case CAMERA_IRIS_MANUAL:
		case CAMERA_IRIS_AUTO:
			addOp(new OpSetAIMode(c, this, r));
			break;
		case CAMERA_WIPER_ONESHOT:
			// FIXME: not yet implemented
			break;
		case RESET_DEVICE:
			addOp(new OpResetCamera(c, this));
			break;
		default:
			break;
		}
	}
}
