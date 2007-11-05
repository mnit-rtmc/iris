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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * TMSEvent is the super-class of all events that are logged within the TMS.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class TMSEvent implements Serializable {

	/** Event description for a SYSTEM RESTARTED event */
	static public final String SYSTEM_RESTARTED = "System RESTARTED";

	/** Description of the event */
	protected String eventDescription;

	/** Additional information about this event */
	protected String remarks;

	/** Unique identifier for this event */
	protected int eventId;

	/** Calendar representing when this event occurred */
	protected Calendar eventCalendar;

	/** The source of the logged event */
	protected String loggedBy;

	/** Constructor for the TMSEvent class */
	public TMSEvent() { }

	/**
	 * Constructor for the TMSEvent class
	 *
	 * @param source            The name of the person that created the event
	 * @param eventDescription  Description of the event
	 * @param calendar          The date and time of the event
	 */
	public TMSEvent(String source, String eventDescription,
		Calendar calendar)
	{
		setEventCalendar(calendar);
		setLoggedBy(source);
		setEventDescription(eventDescription);
	}

	/**
	 * Set the source that logged this TMSEvent
	 *
	 * @param value  The name of the event creator
	 */
	public void setLoggedBy(String value) {
		loggedBy = value;
	}

	/**
	 * Set the calendar object representing when this TMSEvent was logged
	 *
	 * @param value  Time and date of the event
	 */
	public void setEventCalendar(Calendar value) {
		eventCalendar = value;
	}

	/**
	 * Set the calendar object representing when this TMSEvent was logged
	 *
	 * @param value  Time and date of the event
	 */
	public void setEventCalendar(Date value) {
		eventCalendar = Calendar.getInstance();
		eventCalendar.setTime(value);
	}

	/**
	 * Set the unique event id
	 *
	 * @param value  The unique id of the event
	 */
	public void setEventId(int value) {
		eventId = value;
	}

	/**
	 * Set the description of this event
	 *
	 * @param desc  The description of the event
	 */
	public void setEventDescription(String desc) {
		eventDescription = desc;
	}

	/**
	 * Set the remarks for this event
	 *
	 * @param remarks  The new eventRemarks
	 */
	public void setEventRemarks(String remarks) {
		this.remarks = remarks;
	}

	/**
	 * Get the name of the person that logged this TMSEvent
	 *
	 * @return   The user's name
	 */
	public String getLoggedBy() {
		return loggedBy;
	}

	/**
	 * Get the calendar object representing when this TMSEvent was logged
	 *
	 * @return   The event calendar
	 */
	public Calendar getEventCalendar() {
		return eventCalendar;
	}

	/**
	 * Get the unique event id
	 *
	 * @return   The event Id
	 */
	public int getEventId() {
		return eventId;
	}

	/**
	 * Get the description of this event
	 *
	 * @return   The event description
	 */
	public String getEventDescription() {
		return eventDescription;
	}

	/**
	 * Get the remarks for this event
	 *
	 * @return   The event remarks
	 */
	public String getEventRemarks() {
		return remarks;
	}
}
