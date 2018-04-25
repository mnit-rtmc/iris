/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2018  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.HashSet;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
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

	/** Set of all controllers in full-screen mode */
	static private final HashSet<String> FULL = new HashSet<String>();

	/** Update full-screen state of one controller.
	 * @param c Controller ID.
	 * @param f True for full-screen; otherwise false.
	 * @return Previous full-screen state. */
	static private boolean fullScreen(String c, boolean f) {
		synchronized (FULL) {
			return (f) ? !FULL.add(c) : FULL.remove(c);
		}
	}

	/** Get the controller for a video monitor */
	static private ControllerImpl getController(VideoMonitorImpl vm) {
		Controller c = vm.getController();
		return (c instanceof ControllerImpl) ? (ControllerImpl)c : null;
	}

	/** Get video monitor at specified controller pin */
	static private VideoMonitorImpl getMonitor(ControllerImpl c, int p) {
		ControllerIO cio = c.getIO(p);
		return (cio instanceof VideoMonitorImpl)
		      ? (VideoMonitorImpl) cio
		      : null;
	}

	/** Get camera on video monitor at specified controller pin */
	static private CameraImpl getCamera(ControllerImpl c, int p) {
		VideoMonitorImpl vm = getMonitor(c, p);
		if (vm != null) {
			Camera cam = vm.getCamera();
			if (cam instanceof CameraImpl)
				return (CameraImpl) cam;
		}
		return null;
	}

	/** Check if a video monitor is blank */
	static private boolean isBlank(VideoMonitorImpl vm) {
		return (null == vm) || CameraHelper.isBlank(vm.getCamera());
	}

	/** Get monitor pin for full-screen mode */
	static private int fullScreenPin(ControllerImpl c) {
		int max_pin = c.getMaxPin();
		for (int p = 1; p <= max_pin; p++) {
			VideoMonitorImpl vm = getMonitor(c, p);
			if (!isBlank(vm))
				return p;
		}
		return 1;
	}

	/** Get extra monitor numbers for full-screen mode */
	static private String getExtra(ControllerImpl c) {
		StringBuilder sb = new StringBuilder();
		int max_pin = c.getMaxPin();
		for (int p = 1; p <= max_pin; p++) {
			if (p > 1)
				sb.append(' ');
			sb.append(getExtra(getMonitor(c, p)));
		}
		return sb.toString();
	}

	/** Get extra number for one monitor */
	static private String getExtra(VideoMonitorImpl vm) {
		if (vm != null) {
			return isBlank(vm)
			      ? Integer.toString(vm.getMonNum())
			      : "-";
		} else
			return "#";
	}

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
		createOp("video.monitor.op.switch", vm, switchOp(vm, cam));
	}

	/** Create a monitor switch operation */
	private OpStep switchOp(VideoMonitorImpl vm, CameraImpl cam) {
		ControllerImpl c = getController(vm);
		return (c != null)
		      ? new OpStoreMultiple(switchProps(c, vm.getPin(), cam))
		      : null;
	}

	/** Create a list of properties for a switch operation */
	private ArrayList<MonProp> switchProps(ControllerImpl c, int pin,
		CameraImpl cam)
	{
		ArrayList<MonProp> props = new ArrayList<MonProp>();
		boolean fs = c.shouldUseFullScreen();
		boolean pfs = fullScreen(c.getName(), fs);
		if (pfs != fs) {
			props.add(new ConfigProp(0)); // start configuring
			if (fs) {
				int p = fullScreenPin(c);
				props.add(new SwitchProp(1, getCamera(c, p)));
				configFull(props, c);
			} else {
				switchAll(props, c);
				configNormal(props, c);
			}
		} else
			props.add(new SwitchProp(pin, cam));
		return props;
	}

	/** Create a list of properties for all switch operation */
	private void switchAll(ArrayList<MonProp> props, ControllerImpl c) {
		int max_pin = c.getMaxPin();
		for (int p = 1; p <= max_pin; p++) {
			CameraImpl cam = getCamera(c, p);
			props.add(new SwitchProp(p, cam));
		}
	}

	/** Send a device request
	 * @param vm The VideoMonitor object.
	 * @param dr The desired DeviceRequest. */
	@Override
	public void sendRequest(VideoMonitorImpl vm, DeviceRequest dr) {
		switch (dr) {
		case SEND_SETTINGS:
			createOp("video.monitor.op.config", vm, configOp(vm));
			break;
		case QUERY_STATUS:
			addOp(new Operation("video.monitor.op.query",
				getController(vm), new OpStatus(vm)));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Create a monitor configuration operation */
	private OpStep configOp(VideoMonitorImpl vm) {
		ControllerImpl c = getController(vm);
		return (c != null) ? new OpStoreMultiple(configProps(c)) : null;
	}

	/** Create a list of config properties */
	private ArrayList<MonProp> configProps(ControllerImpl c) {
		ArrayList<MonProp> props = new ArrayList<MonProp>();
		// FIXME: props.add(new ConfigProp(0)); // start configuring
		boolean fs = c.shouldUseFullScreen();
		fullScreen(c.getName(), fs);
		if (fs)
			configFull(props, c);
		else
			configNormal(props, c);
		return props;
	}

	/** Create a list of "normal" (not full-screen) config properties */
	private void configNormal(ArrayList<MonProp> props, ControllerImpl c) {
		int max_pin = c.getMaxPin();
		for (int p = 1; p <= max_pin; p++)
			props.add(new MonitorProp(p, getMonitor(c, p)));
		props.add(new ConfigProp(max_pin));
	}

	/** Append full-screen config properties to a list */
	private void configFull(ArrayList<MonProp> props, ControllerImpl c) {
		int pin = fullScreenPin(c);
		props.add(new MonitorProp(1, getMonitor(c, pin), getExtra(c)));
		props.add(new ConfigProp(1));
	}
}
