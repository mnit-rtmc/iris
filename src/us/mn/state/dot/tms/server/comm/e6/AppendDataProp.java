/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.e6;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Append data to asynchronous tag response property.
 *
 * @author Douglas Lau
 */
public class AppendDataProp extends E6Property {

	/** Tag transaction config command */
	static private final Command CMD = new Command(
		CommandGroup.TAG_TRANSACTION_CONFIG);

	/** Store command code */
	static private final int STORE = 0x0002;

	/** Query command code */
	static private final int QUERY = 0x0003;

	/** Append data values */
	public enum Value {
		disabled, date_time_stamp;
		static public Value fromOrdinal(int o) {
			for (Value v: values())
				if (v.ordinal() == o)
					return v;
			return null;
		}
	};

	/** Append data value */
	private Value value = Value.disabled;

	/** Get the command */
	@Override
	public Command command() {
		return CMD;
	}

	/** Get the query packet data */
	@Override
	public byte[] queryData() {
		byte[] d = new byte[2];
		format16(d, 0, QUERY);
		return d;
	}

	/** Parse a received query packet */
	@Override
	public void parseQuery(byte[] d) throws IOException {
		if (d.length != 5)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		Value v = Value.fromOrdinal(d[4]);
		if (v != null)
			value = v;
		else
			throw new ParsingException("BAD APPEND DATA");
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "append data: " + value;
	}
}
