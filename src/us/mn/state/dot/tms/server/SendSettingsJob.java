/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
 * Copyright (C) 2017  Iteris Inc.
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
package us.mn.state.dot.tms.server;

import java.util.Calendar;
import java.util.Iterator;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.DeviceRequest;

/**
 * Job to send settings to all field controllers.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SendSettingsJob extends Job {

	/** Create a new send settings job */
	public SendSettingsJob() {
		super(Calendar.DATE, 1, Calendar.HOUR, 4);
	}

	/** Create a new one-shot send settings job */
	public SendSettingsJob(int ms) {
		super(ms);
	}

	/** Perform the send settings job */
	public void perform() {
		sendSettings();
	}

	/** Send settings to all controllers */
	private void sendSettings() {
		Iterator<Controller> it = ControllerHelper.iterator();
		while (it.hasNext()) {
			Controller c = it.next();
			c.setDeviceRequest(
				DeviceRequest.SEND_SETTINGS.ordinal());
		}
		requestCameraStop();
	}

	/** Send a stop PTZ request to all cameras */
	private void requestCameraStop() {
		Float[] ptz = new Float[3];
		ptz[0] = 0f;
		ptz[1] = 0f;
		ptz[2] = 0f;
		Iterator<Camera> it = CameraHelper.iterator();
		while (it.hasNext()) {
			Camera cam = it.next();
			cam.setPtz(ptz);
		}
	}
}
