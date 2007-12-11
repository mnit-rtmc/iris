/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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

/**
 * A video monitor output from a video switch
 *
 * @author Douglas Lau
 */
public class VideoMonitorImpl extends BaseObjectImpl implements VideoMonitor {

	/** Load all the video monitors */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading video monitors...");
		store.query("SELECT name, description FROM video_monitor;",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new VideoMonitorImpl(
					row.getString(1),	// name
					row.getString(2)	// description
				));
			}
		});
	}

	/** Store a video monitor */
	public void doStore() throws TMSException {
		store.update("INSERT INTO " + getTable() +
			" (name, description) VALUES ('" + name + "', '" +
			description + "');");
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
	protected VideoMonitorImpl(String n, String d) {
		this(n);
		description = d;
	}

	/** Get the integer id of the video monitor */
	public int getUID() {
		try {
			return Integer.parseInt(name.substring(1));
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
}
