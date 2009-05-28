/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.vicon;

/**
 * A request to select a new monitor
 *
 * @author Douglas Lau
 */
public class SelectMonitorRequest extends Request {

	/** Command to select a new monitor */
	static protected final String CODE = "A";

	/** Monitor to select */
	protected final int monitor;

	/** Create a new select monitor request */
	public SelectMonitorRequest(int m) {
		monitor = m;
	}

	/** Get the code to send to the switcher */
	public String toString() {
		return CODE + monitor;
	}
}
