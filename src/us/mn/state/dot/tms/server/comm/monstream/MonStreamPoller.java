/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2020  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.MonitorStyle;
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

	/** Get a monitor style */
	static private MonitorStyle monitorStyle(VideoMonitorImpl mon) {
		return (mon != null)
		      ? mon.getMonitorStyle()
		      : null;
	}

	/** Check if a video monitor is allowed to auto-expand */
	static private boolean getAutoExpand(VideoMonitorImpl mon) {
		MonitorStyle ms = monitorStyle(mon);
		return (ms != null) && ms.getAutoExpand();
	}

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

	/** Check if a controller is full screen */
	static private boolean isFullScreen(String c) {
		synchronized (FULL) {
			return FULL.contains(c);
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

	/** Should full screen mode be used? */
	static private boolean shouldUseFullScreen(ControllerImpl c) {
		int n_mons = 0;
		int n_cams = 0;
		int max_pin = c.getMaxPin();
		for (int p = 1; p <= max_pin; p++) {
			VideoMonitorImpl vm = getMonitor(c, p);
			if (vm != null) {
				if (!getAutoExpand(vm))
					return false;
				n_mons++;
				if (!isBlank(vm)) {
					n_cams++;
					if (n_cams > 1)
						return false;
				}
			}
		}
		return (n_mons > 1);
	}

	/** Get video monitor from a pin (maybe full-screen).
	 * @param c Controller.
	 * @param p Pin on controller.
	 * @param full Full-screen (or only configured pin).
	 * @return Video monitor at specified pin. */
	static public VideoMonitorImpl getMonitor(ControllerImpl c, int p,
		boolean full)
	{
		if (full) {
			int max_pin = c.getMaxPin();
			boolean fs = isFullScreen(c.getName()) || (max_pin <= 1);
			return (fs) ? getMonitor(c, fullScreenPin(c)) : null;
		} else
			return getMonitor(c, p);
	}

	/** Create a new MonStream poller */
	public MonStreamPoller(CommLink link) {
		super(link, UDP, false);
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
		boolean fs = shouldUseFullScreen(c);
		boolean pfs = fullScreen(c.getName(), fs);
		if (pfs != fs)	// Changing mode
			switchModeProps(props, c, fs);
		else if (fs) {
			// Remaining in full-screen mode
			int p = fullScreenPin(c);
			props.add(new SwitchProp(1, getCamera(c, p)));
			props.add(new MonitorProp(1, getMonitor(c, p),
				getExtra(c)));
		} else	// Normal mode
			props.add(new SwitchProp(pin, cam));
		return props;
	}

	/** Add properties for switch operation with full-screen mode change */
	private void switchModeProps(ArrayList<MonProp> props, ControllerImpl c,
		boolean fs)
	{
		props.add(new ConfigProp(0)); // start configuring
		if (fs) {
			int p = fullScreenPin(c);
			props.add(new SwitchProp(1, getCamera(c, p)));
			configFull(props, c);
		} else {
			switchAll(props, c);
			configNormal(props, c);
		}
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
			addOp(new Operation("video.monitor.op.heartbeat",
				getController(vm), new OpHeartbeat()));
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
		props.add(new ConfigProp(0)); // start configuring
		boolean fs = shouldUseFullScreen(c);
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
