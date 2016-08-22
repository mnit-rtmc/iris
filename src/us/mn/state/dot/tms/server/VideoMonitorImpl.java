/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.VideoMonitorPoller;

/**
 * A video monitor device.
 *
 * @author Douglas Lau
 */
public class VideoMonitorImpl extends DeviceImpl implements VideoMonitor {

	/** Check if the camera video should be published */
	static private boolean isCameraPublished(Camera c) {
		return c != null && c.getPublish();
	}

	/** Load all the video monitors */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, VideoMonitorImpl.class);
		store.query("SELECT name, controller, pin, notes, restricted " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new VideoMonitorImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("restricted", restricted);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a video monitor */
	private VideoMonitorImpl(ResultSet row) throws SQLException {
		this(row.getString(1),		// name
		     row.getString(2),		// controller
		     row.getInt(3),		// pin
		     row.getString(4),		// notes
		     row.getBoolean(5)		// restricted
		);
	}

	/** Create a video monitor */
	private VideoMonitorImpl(String n, String c, int p, String nt,
		boolean r)
	{
		this(n, lookupController(c), p, nt, r);
	}

	/** Create a video monitor */
	private VideoMonitorImpl(String n, ControllerImpl c, int p, String nt,
		boolean r)
	{
		super(n, c, p, nt);
		restricted = r;
	}

	/** Create a new video monitor */
	public VideoMonitorImpl(String n) throws TMSException, SonarException {
		super(n);
	}

	/** Flag to restrict publishing camera images */
	private boolean restricted;

	/** Set flag to restrict publishing camera images */
	@Override
	public void setRestricted(boolean r) {
		restricted = r;
	}

	/** Set flag to restrict publishing camera images */
	public void doSetRestricted(boolean r) throws TMSException {
		if (r == restricted)
			return;
		store.update(this, "restricted", r);
		setRestricted(r);
		if (r && !isCameraPublished(camera))
			setCameraNotify(null);
	}

	/** Get flag to restrict publishing camera images */
	@Override
	public boolean getRestricted() {
		return restricted;
	}

	/** Camera displayed on the video monitor */
	private transient Camera camera;

	/** Set the camera displayed on the monitor */
	@Override
	public void setCamera(Camera c) {
		camera = c;
	}

	/** Set the camera displayed on the monitor */
	public void doSetCamera(Camera c) {
		if (restricted && !isCameraPublished(c))
			c = null;
		setCamera(c);
		if (c == null)
			selectBlankCamera();
		else
			selectCamera(c.getName());
	}

	/** Set the camera and notify clients of the change */
	public void setCameraNotify(Camera c) {
		doSetCamera(c);
		notifyAttribute("camera");
	}

	/** Get the camera displayed on the monitor */
	@Override
	public Camera getCamera() {
		return camera;
	}

	/** Get the video monitor poller */
	private VideoMonitorPoller getVideoMonitorPoller() {
		DevicePoller dp = getPoller();
		return (dp instanceof VideoMonitorPoller)
		      ? (VideoMonitorPoller) dp
		      : null;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		VideoMonitorPoller vmp = getVideoMonitorPoller();
		if (vmp != null)
			vmp.sendRequest(this, dr);
	}

	/** Select a blank camera for the video monitor */
	private void selectBlankCamera() {
		selectCamera(SystemAttrEnum.CAMERA_ID_BLANK.getString());
	}

	/** Select a camera for the video monitor */
	private void selectCamera(String cam) {
		Controller c = getController();
		if (c instanceof ControllerImpl)
			selectCamera((ControllerImpl) c, cam);
		else
			selectCameraWithSwitcher(cam);
	}

	/** Select a camera for the video monitor with a switcher */
	private void selectCameraWithSwitcher(String cam) {
		Iterator<Controller> it = ControllerHelper.iterator();
		while (it.hasNext()) {
			Controller c = it.next();
			if (c instanceof ControllerImpl)
				selectCamera((ControllerImpl) c, cam);
		}
	}

	/** Select a camera for the video monitor */
	private void selectCamera(ControllerImpl c, String cam) {
		DevicePoller dp = c.getPoller();
		if (dp instanceof VideoMonitorPoller) {
			VideoMonitorPoller vmp = (VideoMonitorPoller) dp;
			vmp.setMonitorCamera(c, this, cam);
		}
	}
}
