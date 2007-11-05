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
 * Encapsulates the information pertaining to a detector malfunction
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class DetectorMalfunctionEvent extends DeviceEvent {

	/** An event description */
	static public final String CHATTER = "CHATTER";

	/** An event description */
	static public final String LOCKED_ON = "LOCKED ON";

	/** An event description */
	static public final String NO_HITS = "NO HITS";

	/** Constructor for the DetectorMalfunctionEvent class */
	public DetectorMalfunctionEvent() { }

	/**
	 * Constructor for the DetectorMalfunctionEvent class
	 *
	 * @param source            The source that logged this TMSEvent
	 * @param eventDescription  The description of the event
	 * @param deviceType        The type of system device affected
	 * @param deviceId          The unique identifier for the affected device
	 * @param calendar          The date the TMSEvent was logged
	 */
	public DetectorMalfunctionEvent(String source, String eventDescription,
		String deviceType, String deviceId, Calendar calendar)
	{
		super(source, eventDescription, deviceType, deviceId, calendar);
	}
}
