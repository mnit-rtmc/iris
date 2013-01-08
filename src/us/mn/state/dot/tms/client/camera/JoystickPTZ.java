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

/**
 * Joystick handler for PTZ control.  This consists of two threads, one which
 * reads events from a joystick, and the other which polls the PTZ state at
 * regular intervals.
 *
 * @author Douglas Lau
 */
public class JoystickPTZ {

	/** Joystick polling thread */
	static private final JoystickThread joystick = new JoystickThread();

	/** Period (ms) to poll PTZ state */
	static private final int POLL_PERIOD = 200;

	/** Dead zone needed for too-precise joystick drivers */
	static private final float AXIS_DEADZONE = 3f / 64;

	/** Filter an axis to remove slop around the joystick dead zone */
	static private float filter_deadzone(float v) {
		float av = Math.abs(v);
		if(av > AXIS_DEADZONE) {
			float fv = (av - AXIS_DEADZONE) / (1 - AXIS_DEADZONE);
			if(v < 0)
				return -fv;
			else
				return fv;
		} else
			return 0;
	}

	/** Camera PTZ control */
	private final CameraPTZ cam_ptz;

	/** Flag to indicate life */
	private boolean alive = true;

	/** Joystick PTZ polling thread */
	private final Thread joy_poller = new Thread() {
		public void run() {
			while(alive) {
				try {
					pollJoystick();
					sleep(POLL_PERIOD);
				}
				catch(InterruptedException e) {
					break;
				}
			}
		}
	};

	/** Pan value from last update */
	private float pan = 0;

	/** Tilt value from last update */
	private float tilt = 0;

	/** Zoom value from last update */
	private float zoom = 0;

	/** Create a new joystick PTZ poller */
	public JoystickPTZ(CameraPTZ cptz) {
		cam_ptz = cptz;
		joy_poller.setDaemon(true);
		joy_poller.start();
	}

	/** Poll the joystick and send PTZ command to server */
	private void pollJoystick() {
		float p = filter_deadzone(joystick.getPan());
		float t = -filter_deadzone(joystick.getTilt());
		float z = filter_deadzone(joystick.getZoom());
		if(p != pan || t != tilt || z != zoom)
			cam_ptz.sendPtz(p, t, z);
		pan = p;
		tilt = t;
		zoom = z;
	}

	/** Add a joystick listener */
	public void addJoystickListener(JoystickListener l) {
		joystick.addJoystickListener(l);
	}

	/** Dispose of the joystick threads */
	public void dispose() {
		alive = false;
		joystick.clearJoystickListeners();
	}
}
