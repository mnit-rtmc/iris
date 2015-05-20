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
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Time Property
 *
 * @author Douglas Lau
 */
public class TimeProperty extends SS105Property {

	/** Time stamp */
	protected long stamp = TimeSteward.currentTimeMillis();

	/** Check if the request has a checksum */
	protected boolean hasChecksum() {
		return false;
	}

	/** Format a basic "GET" request */
	protected String formatGetRequest() {
		return "SB";
	}

	/** Format a basic "SET" request */
	protected String formatSetRequest() {
		stamp = TimeSteward.currentTimeMillis();
		int seconds = TimeStamp.secondsSinceEpoch(new Date(stamp));
		return "S4" + HexString.format(seconds, 8);
	}

	/** Parse the response to a QUERY */
	protected void parseQuery(String res) throws IOException {
		Date date = TimeStamp.parse(res);
		stamp = date.getTime();
	}

	/** Parse the response to a STORE */
	protected void parseStore(String res) throws IOException {
		if(!res.equals("Success"))
			throw new ControllerException("Time set error");
	}

	/** Get the sensor time */
	public Date getTime() {
		return new Date(stamp);
	}

	/** Get a string representation */
	public String toString() {
		return "Time " + getTime();
	}
}
