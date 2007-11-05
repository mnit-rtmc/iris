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
 * Abstract super-class of all Events which affect a TMS device
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
abstract public class DeviceEvent extends TMSEvent {

	/** The type of device that the event occurred upon. */
	private String deviceType;

	/** A new unique identifier of the device (if it exists) */
	private String deviceId;

	/** Constructor for the DeviceEvent class */
	public DeviceEvent() { }

	/**
	 * Constructor for the DeviceEvent class
	 *
	 * @param source            The source that logged this TMSEvent
	 * @param eventDescription  The description of the event
	 * @param deviceType        The type of system device affected
	 * @param deviceId          The unique identifier for the affected device
	 * @param calendar          The date the TMSEvent was logged
	 */
	public DeviceEvent(String source, String eventDescription,
		String deviceType, String deviceId, Calendar calendar)
	{
		super(source, eventDescription, calendar);
		setDeviceType(deviceType);
		setDeviceId(deviceId);
	}

	/**
	 * Set the type of device affected by this event
	 *
	 * @param value  The new deviceType value
	 */
	public void setDeviceType(String value) {
		deviceType = value;
	}

	/**
	 * Set the Id of the device affected by this event
	 *
	 * @param value  The new deviceId value
	 */
	public void setDeviceId(String value) {
		deviceId = value;
	}

	/**
	 * Get the type of device affected by this event
	 *
	 * @return   The deviceType value
	 */
	public String getDeviceType() {
		return deviceType;
	}

	/**
	 * Get the Id of the device affected by this event
	 *
	 * @return   The deviceId value
	 */
	public String getDeviceId() {
		return deviceId;
	}
}
