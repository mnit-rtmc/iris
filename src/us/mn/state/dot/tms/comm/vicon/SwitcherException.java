/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.vicon;

import us.mn.state.dot.tms.comm.SerialIOException;

/**
 * A switcher exception is thrown when an error code is received from the
 * switcher.
 *
 * @author Douglas Lau
 */
public class SwitcherException extends SerialIOException {

	/** Create a new switcher exception with the a detail message */
	public SwitcherException(String message) {
		super("VICON ERROR: " + message);
	}
}
