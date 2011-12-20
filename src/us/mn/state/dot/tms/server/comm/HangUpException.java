/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import java.io.EOFException;

/**
 * HangUpException is thrown when a modem hangs up.
 *
 * @author Douglas Lau
 */
public class HangUpException extends EOFException {

	/** Exception Message */
	static public final String MESSAGE = "HANG UP";

	/** Create a new hang up exception */
	public HangUpException() {
		super(MESSAGE);
	}
}
