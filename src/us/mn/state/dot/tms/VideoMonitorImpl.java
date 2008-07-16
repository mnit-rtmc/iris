/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.comm.vicon.SelectMonitorCamera;

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
				namespace.add(new VideoMonitorImpl(
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

	/** Get the integer id of the video monitor */
	public int getUID() {
		String id = name;
		while(!Character.isDigit(id.charAt(0))){
			id = id.substring(1);
		}
		try {
			return Integer.parseInt(id);
		}
		catch(NumberFormatException e) {
			return 0;
		}
	}

	/** Description of video monitor */
	protected String description;

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
	}

	/** Get flag to restrict publishing camera images */
	public boolean getRestricted() {
		return restricted;
	}
}
