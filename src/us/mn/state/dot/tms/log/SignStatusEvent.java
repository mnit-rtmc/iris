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
 * This class encapsulates information pertaining to the deployment and clearing
 * of LCSs and DMSs.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class SignStatusEvent extends DeviceEvent {

	/** A type of status change */
	static public final int TURNED_ON = 1;

	/** A type of status change */
	static public final int TURNED_OFF = 2;

	/** Event description for a Sign Deployed event */
	static public final String SIGN_DEPLOYED = "Sign DEPLOYED";

	/** Event description for a Sign Cleared event */
	static public final String SIGN_CLEARED = "Sign CLEARED";

	/** Event description for a Sign ERROR event */
	static public final String SIGN_ERROR = "Sign ERROR";

	/** The status of the device after the event occurred */
	private int status;

	/** The message displayed after the event took place */
	String message;

	/** Constructor for a sign status event */
	public SignStatusEvent() { }

	/**
	 * Constructor for a sign status event
	 *
	 * @param source            The user who controlled the sign
	 * @param eventDescription  The description of the event
	 * @param status            The status of the sign after the event took place
	 * @param deviceType        The type of sign ( DMS or LCS )
	 * @param deviceId          The IRIS id of the sign
	 * @param msg               The message on the sign after the event took place
	 * @param calendar          The date and time that the event occurred
	 */
	public SignStatusEvent(String source, String eventDescription,
		int status, String deviceType, String deviceId, String msg,
		Calendar calendar)
	{
		super(source, eventDescription, deviceType, deviceId, calendar);
		setMessage(msg);
		this.status = status;
	}

	/**
	 * Set the status of the device after the event occurred
	 *
	 * @param status  The status of the device after the event.
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Get the status of the device after the event occurred
	 *
	 * @return   The status of the device after the event occurred.
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Set the message field of the event
	 *
	 * @param msg  The message displayed on the sign after the event
	 */
	public void setMessage(String msg) {
		message = msg;
	}

	/**
	 * Get the message field of the event
	 *
	 * @return   The message the sign displayed after the event occurred
	 */
	public String getMessage() {
		return message;
	}
}
