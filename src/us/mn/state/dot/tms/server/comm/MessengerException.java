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
package us.mn.state.dot.tms.server.comm;

/**
 * MessengerException is thrown when a Messenger cannot be created.
 *
 * @author Douglas Lau
 */
public class MessengerException extends Exception {

	/** Create a new messenger exception */
	public MessengerException(String msg) {
		super(msg);
	}

	/** Create a new messenger exception */
	public MessengerException(Exception cause) {
		super(cause);
	}
}
