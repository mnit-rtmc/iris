/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss105;

import java.io.IOException;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Time Interval Property
 *
 * @author Douglas Lau
 */
public class TimeIntervalProperty extends MemoryProperty {

	/** Create a new time interval property */
	public TimeIntervalProperty() {
		this(0);
	}

	/** Create a new time interval property */
	public TimeIntervalProperty(int i) {
		value = i;
	}

	/** Current value of time interval */
	int value;

	/** Get the SS105 memory buffer address */
	protected int memoryAddress() {
		return 0x00008E;
	}

	/** Get the SS105 memory buffer length */
	protected short memoryLength() {
		return 8;
	}

	/** Format the buffer to write to SS105 memory */
	protected String formatBuffer() {
		return HexString.format(value, 8);
	}

	/** Parse the response to a QUERY */
	protected void parseQuery(String res) throws IOException {
		try {
			value = Integer.parseInt(res, 16);
		}
		catch(NumberFormatException e) {
			throw new ParsingException(
				"Invalid time interval: " + res);
		}
	}

	/** Get a string representation of the request */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Time interval: ");
		sb.append(value);
		return sb.toString();
	}
}
