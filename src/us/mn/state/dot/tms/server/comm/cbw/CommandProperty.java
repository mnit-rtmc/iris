/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cbw;

/**
 * CBW relay command property.
 *
 * @author Douglas Lau
 */
public class CommandProperty extends CBWProperty {

	/** Create a request string */
	static private String requestString(int relay, boolean on) {
		return "state.xml?relay" + relay + "State=" +
			(on ? "1" : "0");
	}

	/** Create a new relay command property.
	 * @param relay (1-8).
	 * @param on Turn relay on (true) or off (false). */
	public CommandProperty(int relay, boolean on) {
		super(requestString(relay, on));
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "command: " + getPath();
	}
}
