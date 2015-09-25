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
 * Buffered tag transaction count property.
 *
 * @author Douglas Lau
 */
public class BufferedCountProp extends E6Property {

	/** System information command */
	static private final Command CMD =new Command(CommandGroup.SYSTEM_INFO);

	/** Store command code */
	static private final int STORE = 0x0009;

	/** Query command code */
	static private final int QUERY = 0x0008;

	/** Count of buffered tag transactions */
	private int count = 0;

	/** Get the buffered tag transaction count */
	public int getCount() {
		return count;
	}

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
		if (d.length != 9)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		count = parse32(d, 4);
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		byte[] d = new byte[4];
		format16(d, 0, STORE);
		format16(d, 2, 0xA5A5);	// magic word to clear buffer
		return d;
	}

	/** Parse a received store packet */
	@Override
	public void parseStore(byte[] d) throws IOException {
		if (d.length != 4)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != STORE)
			throw new ParsingException("SUB CMD");
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "buffered tag transaction count: " + count;
	}
}
