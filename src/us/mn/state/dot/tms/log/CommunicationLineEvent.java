/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.log;

import java.util.Calendar;

/**
 * This class is used to log communication issues such as loss of comm
 * to a device, comm restored and various other comm errors.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class CommunicationLineEvent extends TMSEvent {

	/** Event description for a Comm ERROR event */
	static public final String COMM_ERROR = "Comm ERROR";

	/** Event description for a Comm FAILED event */
	static public final String COMM_FAILED = "Comm FAILED";

	/** Event description for a Comm RESTORED event */
	static public final String COMM_RESTORED = "Comm RESTORED";

	/** The id of the comm line affected by this event */
	private int line;

	/** The drop number (if drop specific) */
	private int drop;

	/** Device id at this drop (if drop specific) */
	private String deviceId = "";

	/** Constructor for the CommunicationLineEvent */
	public CommunicationLineEvent() { }

	/**
	 * Constructor for the CommunicationLineEvent
	 *
	 * @param eventDescription  The description of the event
	 * @param calendar          The date and time the event occurred
	 * @param line              The id of the affected comm line
	 * @param drop              The drop ( if drop specific )
	 */
	public CommunicationLineEvent(String eventDescription,
		Calendar calendar, int line, int drop, String deviceId)
	{
		super("System", eventDescription, calendar);
		setLine(line);
		setDrop(drop);
		setDeviceId(deviceId);
	}

	/**
	 * Set the id of the comm line affected by this event
	 *
	 * @param id  The id of the comm line
	 */
	public void setLine(int id) {
		line = id;
	}

	/**
	 * Set the drop
	 *
	 * @param drop  The drop number
	 */
	public void setDrop(int drop) {
		this.drop = drop;
	}

	/**
	 * Set the device id
	 *
	 * @param id  The id of the affected device
	 */
	public void setDeviceId(String id) {
		if(id == null)
			deviceId = "";
		else
			deviceId = id;
	}

	/**
	 * Get the id of the comm line affected by this event
	 *
	 * @return   The id of the comm line affected by this event
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Get the drop affected by this event
	 *
	 * @return   The drop value
	 */
	public int getDrop() {
		return drop;
	}

	/**
	 * Get the device id
	 *
	 * @return   The deviceId value
	 */
	public String getDeviceId() {
		return deviceId;
	}
}
