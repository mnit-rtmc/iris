/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2017  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
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
package us.mn.state.dot.tms.client.camera;

import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.client.Session;

/**
 * Camera PTZ control.  This is required to ensure that all continous camera
 * operations (e.g. PTZ move, focus move, iris move) are stopped before
 * switching cameras.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class CameraPTZ {

	/** User session */
	private final Session session;

	/** Camera to send PTZ control */
	private Camera camera;

	/** Pan/tilt/zoom values from most recent PTZ update */
	private final Float[] ptz = new Float[3];

	/** Is focus moving? */
	private boolean focusMoving = false;

	/** Is iris moving? */
	private boolean irisMoving = false;

	/** Is camera control currently enabled? */
	private boolean controlEnabled = true;

	/** Create a new camera PTZ control */
	public CameraPTZ(Session s) {
		session = s;
	}

	/** Can a ptz control be made */
	public boolean canControlPtz() {
		return session.isWritePermitted(camera, "ptz");
	}

	/** Can a device request be made */
	public boolean canRequestDevice() {
		return session.isWritePermitted(camera, "deviceRequest");
	}

	/** Can presets be recalled */
	public boolean canRecallPreset() {
		return session.isWritePermitted(camera, "recallPreset");
	}

	/** Can presets be stored */
	public boolean canStorePreset() {
		return session.isWritePermitted(camera, "storePreset");
	}

	/**
	 * Enable/disable control.  Allows camera controllability to be
	 * toggled.  Does not affect clearMovement().
	 *
	 * @param en true to allow control, false to block control
	 */
	public synchronized void enableControl(boolean en) {
		controlEnabled = en;
	}

	/** Is camera control currently enabled? */
	public boolean isControlEnabled() {
		return controlEnabled;
	}

	/** Is a camera currently selected? */
	public boolean isCameraSelected() {
		return (camera != null);
	}

	/** Set the camera */
	public synchronized void setCamera(Camera c) {
		if (camera != c) {
			clearMovement();
			camera = c;
		}
	}

	/** Get the camera */
	public Camera getCamera() {
		return camera;
	}

	/**
	 * Send a PTZ command to the current camera, subject to the conditions
	 * of doSendPtz().
	 * Observes controlEnabled.
	 */
	public void sendPtz(float p, float t, float z) {
		if (controlEnabled)
			doSendPtz(p,t,z);
	}

	/**
	 * Send a PTZ command to the current camera, unless it is a full-stop
	 * and the camera is already fully stopped.
	 */
	private synchronized void doSendPtz(float p, float t, float z) {
		if (canControlPtz()) {
			if (p != 0 || t != 0 || z != 0 || ptzMoving()) {
				ptz[0] = p;
				ptz[1] = t;
				ptz[2] = z;
				camera.setPtz(ptz);
			}
		}
	}

	/** Was the most recent PTZ update a move? */
	private boolean ptzMoving() {
		return ptz[0] != 0 || ptz[1] != 0 || ptz[2] != 0;
	}

	/**
	 * Send a device request to the current camera.
	 * Observes controlEnabled.
	 */
	public void sendRequest(DeviceRequest dr) {
		if (controlEnabled)
			doSendRequest(dr);
	}

	/**
	 * Send a device request to the current camera, tracking focus/iris
	 * movement states.
	 */
	private synchronized void doSendRequest(DeviceRequest dr) {
		if (canRequestDevice()) {
			updateFocusAndIris(dr);
			camera.setDeviceRequest(dr.ordinal());
		}
	}

	/** Update focus and iris state */
	private void updateFocusAndIris(DeviceRequest dr) {
		switch (dr) {
		case CAMERA_FOCUS_NEAR:
		case CAMERA_FOCUS_FAR:
			focusMoving = true;
			break;
		case CAMERA_FOCUS_STOP:
			focusMoving = false;
			break;
		case CAMERA_IRIS_OPEN:
		case CAMERA_IRIS_CLOSE:
			irisMoving = true;
			break;
		case CAMERA_IRIS_STOP:
			irisMoving = false;
			break;
		default:
			break;
		}
	}

	/**
	 * Stop any movement for the current camera (regardless of the value
	 * of controlEnabled) and clear the movement state variables.
	 */
	public synchronized void clearMovement() {
		doSendPtz(0, 0, 0);
		if (focusMoving)
			doSendRequest(DeviceRequest.CAMERA_FOCUS_STOP);
		if (irisMoving)
			doSendRequest(DeviceRequest.CAMERA_IRIS_STOP);
		clearState();
	}

	/** Ensure states are cleared */
	private void clearState() {
		ptz[0] = 0f;
		ptz[1] = 0f;
		ptz[2] = 0f;
		focusMoving = false;
		irisMoving = false;
	}

	/**
	 * Recall a camera preset.
	 * Observes controlEnabled.
	 */
	public synchronized void recallPreset(int p) {
		if (controlEnabled && canRecallPreset()) {
			camera.setRecallPreset(p);
			clearState();
		}
	}

	/**
	 * Store a camera preset.
	 * Observes controlEnabled.
	 */
	public synchronized void storePreset(int p) {
		if (controlEnabled && canStorePreset()) {
			camera.setStorePreset(p);
			clearState();
		}
	}
}
