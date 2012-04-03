/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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

/**
 * A utility class to controll travel time logging.
 *
 * @author Douglas Lau
 */
public class TravelTime {

	/** Don't allow instantiation */
	private TravelTime() { }

	/** Travel time debug log */
	static private final IDebugLog TRAVEL_LOG = new IDebugLog("travel");

	/** Check if we're logging */
	static public boolean isLogging() {
		return TRAVEL_LOG.isOpen();
	}

	/** Log a message to the travel debug log */
	static public void log(String msg) {
		TRAVEL_LOG.log(msg);
	}
}
