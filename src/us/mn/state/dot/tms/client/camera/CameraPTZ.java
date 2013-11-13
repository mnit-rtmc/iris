/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.Session;

/**
 * Camera PTZ control.  This is required to ensure that all PTZ is stopped
 * before switching cameras.
 *
 * @author Douglas Lau
 */
public class CameraPTZ {

	/** User session */
	private final Session session;

	/** Camera to send PTZ control */
	private Camera camera;

	/** Pan/tilt/zoom values from last update */
	private final Float[] ptz = new Float[3];

	/** Create a new camera PTZ control */
	public CameraPTZ(Session s) {
		session = s;
	}

	/** Can a ptz control be made */
	public boolean canControlPtz() {
		return session.isUpdatePermitted(camera, "ptz");
	}

	/** Set the camera */
	public synchronized void setCamera(Camera c) {
		if(camera != c) {
			clearPtz();
			camera = c;
		}
	}

	/** Send PTZ command to current camera */
	public synchronized void sendPtz(float p, float t, float z) {
		if(canControlPtz()) {
			if(p != 0 || t != 0 || z != 0 || previousPtz()) {
				ptz[0] = p;
				ptz[1] = t;
				ptz[2] = z;
				camera.setPtz(ptz);
			}
		}
	}

	/** Was previous update a PTZ? */
	private boolean previousPtz() {
		return ptz[0] != 0 || ptz[1] != 0 || ptz[2] != 0;
	}

	/** Clear PTZ command to current camera */
	public synchronized void clearPtz() {
		sendPtz(0, 0, 0);
		// Make sure ptz is clear even if there was no camera
		ptz[0] = 0f;
		ptz[1] = 0f;
		ptz[2] = 0f;
	}
}
