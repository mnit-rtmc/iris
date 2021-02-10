/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 * A TMSException is a generic exception which is thrown whenever there is
 * any problems accessing TMS objects.
 *
 * @author Douglas Lau
 */
public class TMSException extends Exception {

	/** Create a new TMS exception */
	public TMSException(String msg) {
		super(msg);
	}

	/** Create a TMS exception with a given cause */
	public TMSException(Throwable cause) {
		super(cause);
	}
}
