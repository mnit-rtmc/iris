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
 * Seen count property.
 *
 * @author Douglas Lau
 */
public class SeenCountProp extends E6Property {

	/** Mode command */
	static private final Command CMD = new Command(CommandGroup.MODE);

	/** Store command code */
	static private final int STORE = 0x0066;

	/** Query command code */
	static private final int QUERY = 0x0067;

	/** RF protocol */
	private final RFProtocol protocol;

	/** Seen count frames */
	private int seen;

	/** Unique count frames */
	private int unique;

	/** Create a seen count property */
	public SeenCountProp(RFProtocol p, int s, int u) {
		protocol = p;
		seen = s;
		unique = u;
	}

	/** Create a seen count property */
	public SeenCountProp(RFProtocol p) {
		this(p, 0, 0);
	}

	/** Get the command */
	@Override
	public Command command() {
		return CMD;
	}

	/** Get the query packet data */
	@Override
	public byte[] queryData() {
		byte[] d = new byte[3];
		format16(d, 0, QUERY);
		format8(d, 2, protocol.ordinal());
		return d;
	}

	/** Parse a received query packet */
	@Override
	public void parseQuery(byte[] d) throws IOException {
		if (d.length != 9)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		if (RFProtocol.fromOrdinal(parse8(d, 4)) != protocol)
			throw new ParsingException("RF PROTOCOL");
		seen = parse16(d, 5);
		unique = parse16(d, 7);
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		byte[] d = new byte[7];
		format16(d, 0, STORE);
		format8(d, 2, protocol.ordinal());
		format16(d, 3, seen);
		format16(d, 5, unique);
		return d;
	}

	/** Parse a received store packet */
	@Override
	public void parseStore(byte[] d) throws IOException {
		if (d.length != 5)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != STORE)
			throw new ParsingException("SUB CMD");
		if (RFProtocol.fromOrdinal(parse8(d, 4)) != protocol)
			throw new ParsingException("RF PROTOCOL");
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return protocol + " seen cnt: " + seen + ", unique: " + unique;
	}
}
