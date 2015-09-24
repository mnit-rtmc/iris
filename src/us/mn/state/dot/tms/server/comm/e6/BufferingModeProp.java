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
 * Buffering mode property.
 *
 * @author Douglas Lau
 */
public class BufferingModeProp extends E6Property {

	/** System information command */
	static private final Command CMD =new Command(CommandGroup.SYSTEM_INFO);

	/** Store command code */
	static private final int STORE = 0x0016;

	/** Query command code */
	static private final int QUERY = 0x0017;

	/** Buffering enabled */
	private boolean enabled;

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
		enabled = parse8(d, 4) != 0;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "buffered transaction mode: " + enabled;
	}
}
