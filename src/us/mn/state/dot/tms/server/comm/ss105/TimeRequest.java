/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2010  Minnesota Department of Transportation
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
import java.util.Date;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Time Request
 *
 * @author Douglas Lau
 */
public class TimeRequest extends Request {

	/** Is this a SET request */
	protected boolean is_set = false;

	/** Time stamp */
	protected long stamp = System.currentTimeMillis();

	/** Check if the request has a checksum */
	protected boolean hasChecksum() {
		return false;
	}

	/** Format a basic "GET" request */
	protected String formatGetRequest() {
		is_set = false;
		return "SB";
	}

	/** Format a basic "SET" request */
	protected String formatSetRequest() {
		is_set = true;
		stamp = System.currentTimeMillis();
		int seconds = TimeStamp.seconds(new Date(stamp));
		return "S4" + hex(seconds, 8);
	}

	/** Set the response to the request */
	protected void setResponse(String r) throws IOException {
		if(is_set) {
			if(r.equals("Success"))
				return;
			else
				throw new ControllerException("Time set error");
		} else
			parseGetResponse(r);
	}

	/** Parse the response to a GET request */
	protected void parseGetResponse(String r) throws IOException {
		Date date = TimeStamp.parse(r);
		stamp = date.getTime();
	}

	/** Get the sensor time */
	public Date getTime() {
		return new Date(stamp);
	}

	/** Get a string representation of the request */
	public String toString() {
		return "Time " + getTime();
	}
}
