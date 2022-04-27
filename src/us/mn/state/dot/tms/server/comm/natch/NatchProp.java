/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.natch;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import us.mn.state.dot.tms.server.comm.ControllerProp;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.utils.HexString;

/**
 * Property for Natch protocol
 *
 * @author Douglas Lau
 */
abstract public class NatchProp extends ControllerProp {

	/** UTF-8 charset */
	static protected final Charset UTF8 = Charset.forName("UTF-8");

	/** Date formatter for RFC 3339 */
	static protected final String RFC3339 = "yyyy-MM-dd'T'HH:mm:ssXXX";

	/** Parse a boolean value */
	static protected boolean parseBool(String v) {
		return v.equals("1");
	}

	/** Parse a positive integer value */
	static protected int parseInt(String v) {
		try {
			return Integer.parseInt(v);
		}
		catch (NumberFormatException e) {
			return -1;
		}
	}

	/** Parse a date/time stamp */
	static protected long parseStamp(String v) {
		try {
			return new SimpleDateFormat(RFC3339).parse(v).getTime();
		}
		catch (ParseException e) {
			return 0;
		}
	}

	/** Message ID */
	protected final String message_id;

	/** Create a new natch property */
	protected NatchProp(Counter c) {
		message_id = HexString.format(c.next(), 4);
	}

	/** Parse one received message */
	boolean parseMsg(String msg) throws IOException {
		String[] param = msg.split(",");
		if (param.length >= 2 &&
		    param[0].equals(code()) &&
		    checkMessageId(param[1]))
		{
			if (param.length != parameters()) {
				throw new ParsingException("Wrong params: " +
					code() + " (" + param.length + ')');
			}
			return parseParams(param);
		}
		return false;
	}

	/** Check received message ID */
	protected boolean checkMessageId(String msg_id) {
		return msg_id.equals(message_id);
	}

	/** Get the message code */
	abstract protected String code();

	/** Get the number of response parameters */
	abstract protected int parameters();

	/** Parse parameters for a received message */
	abstract protected boolean parseParams(String[] param);
}
