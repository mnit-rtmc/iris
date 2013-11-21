/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2013  Minnesota Department of Transportation
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;

/**
 * Joystick event driver for controlling camera PTZ
 *
 * @author Douglas Lau
 */
public final class JoystickThread {

	/** Regular expression to match axis events */
	static protected final Pattern AXIS =
		Pattern.compile("axis:(\\d),value:([-0-9.]+)");

	/** Regular expression to match button events */
	static protected final Pattern BUTTON =
		Pattern.compile("button:(\\d+),value:([01])");

	/** Pan (X) axis */
	static protected final int AXIS_PAN = 0;

	/** Tilt (Y) axis */
	static protected final int AXIS_TILT = 1;

	/** Zoom (Z) axis */
	static protected final int AXIS_ZOOM = 2;

	/** Sub-process to run joystick script */
	private Process process;

	/** Flag to indicate life */
	private boolean alive = true;

	/** Joystick event thread */
	private final Thread joy_event = new Thread() {
		public void run() {
			try {
				process = createProcess();
				if(process != null)
					readEvents();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			finally {
				if(process != null)
					process.destroy();
			}
		}
	};

	/** Create a new joystick thread */
	public JoystickThread() {
		joy_event.setDaemon(true);
		joy_event.start();
	}

	/** Create joystick sub-process */
	private Process createProcess() throws IOException {
		JoyScript joy = new JoyScript();
		return joy.createProcess();
	}

	/** Read joystick events */
	private void readEvents() throws IOException {
		InputStream is = process.getInputStream();
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(is));
		while(alive) {
			String line = reader.readLine();
			if(line == null)
				break;
			else
				parseEvent(line);
		}
	}

	/** Parse one joystick event */
	private void parseEvent(String ev) {
		Matcher m = AXIS.matcher(ev);
		if(m.matches()) {
			int axis = Integer.parseInt(m.group(1));
			float value = Float.parseFloat(m.group(2));
			setAxis(axis, value);
		}
		m = BUTTON.matcher(ev);
		if(m.matches()) {
			int button = Integer.parseInt(m.group(1));
			int value = Integer.parseInt(m.group(2));
			setButton(button, value != 0);
		}
	}

	/** Set the value of one axis */
	protected void setAxis(int axis, float value) {
		if(axis == AXIS_PAN)
			setPan(value);
		else if(axis == AXIS_TILT)
			setTilt(value);
		else if(axis == AXIS_ZOOM)
			setZoom(value);
	}

	/** Current pan value */
	protected float pan;

	/** Set the pan value */
	protected void setPan(float p) {
		pan = p;
	}

	/** Get the current pan value */
	public float getPan() {
		return pan;
	}

	/** Current tilt value */
	protected float tilt;

	/** Set the tilt value */
	protected void setTilt(float t) {
		tilt = t;
	}

	/** Get the current tilt value */
	public float getTilt() {
		return tilt;
	}

	/** Current zoom value */
	protected float zoom;

	/** Set the zoom value */
	protected void setZoom(float z) {
		zoom = z;
	}

	/** Get the current zoom value */
	public float getZoom() {
		return zoom;
	}

	/** Set the value of one button */
	protected void setButton(int button, boolean pressed) {
		final JoystickButtonEvent ev = new JoystickButtonEvent(this,
			button, pressed);
		runSwing(new Runnable() {
			public void run() {
				fireJoystickButtonEvent(ev);
			}
		});
	}

	/** The listeners of this model */
	private final List<JoystickListener> listeners =
		new LinkedList<JoystickListener>();

	/** Add a joystick listener */
	public void addJoystickListener(JoystickListener l) {
		synchronized(listeners) {
			listeners.add(l);
		}
	}

	/** Remove a joystick listener */
	public void removeJoystickListener(JoystickListener l) {
		synchronized(listeners) {
			listeners.remove(l);
		}
	}

	/** Clear all joystick listeners */
	public void clearJoystickListeners() {
		synchronized(listeners) {
			listeners.clear();
		}
	}

	/** Fire a joystick event to all listeners */
	private void fireJoystickButtonEvent(JoystickButtonEvent e) {
		synchronized(listeners) {
			for(JoystickListener l: listeners)
				l.buttonChanged(e);
		}
	}

	/** Dispose of the joystick thread */
	public void dispose() {
		alive = false;
		Process p = process;
		if(p != null)
			p.destroy();
	}
}
