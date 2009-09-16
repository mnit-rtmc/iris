/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.VideoMonitorPoller;

/**
 * A video monitor output from a video switch
 *
 * @author Douglas Lau
 */
public class VideoMonitorImpl extends BaseObjectImpl implements VideoMonitor {

	/** Load all the video monitors */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading video monitors...");
		namespace.registerType(SONAR_TYPE, VideoMonitorImpl.class);
		store.query("SELECT name, description, restricted " +
			"FROM video_monitor;", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new VideoMonitorImpl(
					row.getString(1),	// name
					row.getString(2),	// description
					row.getBoolean(3)	// restricted
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("description", description);
		map.put("restricted", restricted);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new video monitor */
	public VideoMonitorImpl(String n) {
		super(n);
	}

	/** Create a new video monitor */
	protected VideoMonitorImpl(String n, String d, boolean r) {
		this(n);
		description = d;
		restricted = r;
	}

	/** Description of video monitor */
	protected String description = "";

	/** Set the video monitor description */
	public void setDescription(String d) {
		description = d;
	}

	/** Set the video monitor description */
	public void doSetDescription(String d) throws TMSException {
		if(d.equals(description))
			return;
		store.update(this, "description", d);
		setDescription(d);
	}

	/** Get the video monitor description */
	public String getDescription() {
		return description;
	}

	/** Flag to restrict publishing camera images */
	protected boolean restricted;

	/** Set flag to restrict publishing camera images */
	public void setRestricted(boolean r) {
		restricted = r;
	}

	/** Set flag to restrict publishing camera images */
	public void doSetRestricted(boolean r) throws TMSException {
		if(r == restricted)
			return;
		store.update(this, "restricted", r);
		setRestricted(r);
		if(r && !isCameraPublished(camera))
			selectCamera("");
	}

	/** Get flag to restrict publishing camera images */
	public boolean getRestricted() {
		return restricted;
	}

	/** Camera displayed on the video monitor */
	protected transient Camera camera;

	/** Set the camera displayed on the monitor */
	public void setCamera(Camera c) {
		camera = c;
	}

	/** Set the camera displayed on the monitor */
	public void doSetCamera(Camera c) throws TMSException {
		if(restricted && !isCameraPublished(c))
			c = null;
		setCamera(c);
		if(c == null)
			selectCamera("");
		else
			selectCamera(c.getName());
	}

	/** Get the camera displayed on the monitor */
	public Camera getCamera() {
		return camera;
	}

	/** Check if the camera video should be published */
	static protected boolean isCameraPublished(Camera c) {
		return c != null && c.getPublish();
	}

	/** Select a camera for the video monitor */
	public void selectCamera(final String cam) {
		ControllerHelper.find(new Checker<Controller>() {
			public boolean check(Controller c) {
				if(c instanceof ControllerImpl)
					selectCamera((ControllerImpl)c, cam);
				return false;
			}
		});
	}

	/** Select a camera for the video monitor */
	protected void selectCamera(ControllerImpl c, String cam) {
		MessagePoller p = c.getPoller();
		if(p instanceof VideoMonitorPoller) {
			VideoMonitorPoller vmp = (VideoMonitorPoller)p;
			vmp.setMonitorCamera(c, this, cam);
		}
	}
}
