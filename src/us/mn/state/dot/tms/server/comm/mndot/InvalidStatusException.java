/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Invalid metering status exception
 *
 * @author Douglas Lau
 */
public class InvalidStatusException extends ParsingException {

	/** Create a new invalid status exception */
	public InvalidStatusException(int s) {
		super("INVALID METERING STATUS: " + s);
	}

	/** Create a new invalid status exception.
	 * @param s Meter status code.
	 * @param r Meter rate index. */
	public InvalidStatusException(int s, int r) {
		super("INVALID METERING STATE: " + s + ", " + r);
	}
}
