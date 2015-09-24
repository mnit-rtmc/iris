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
 * Protocol property.
 *
 * @author Douglas Lau
 */
public class ProtocolProp extends E6Property {

	/** Mode command */
	static private final Command CMD = new Command(CommandGroup.MODE);

	/** Store command code */
	static private final int STORE = 0x0003;

	/** Query command code */
	static private final int QUERY = 0x0004;

	/** Protocol bits */
	private int bits;

	/** Get array of RF protocols */
	public RFProtocol[] getArray() {
		return RFProtocol.fromBits(bits);
	}

	/** Create a protocol property */
	public ProtocolProp(RFProtocol[] p) {
		int b = 0;
		for (RFProtocol pp: p)
			b |= pp.bit;
		bits = b;
	}

	/** Create a protocol property */
	public ProtocolProp() {
		this(new RFProtocol[0]);
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
		if (d.length != 6)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		bits = parse16(d, 4);
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		byte[] d = new byte[4];
		format16(d, 0, STORE);
		format16(d, 2, bits);
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
		StringBuilder sb = new StringBuilder();
		sb.append("protocols: ");
		for (RFProtocol p: getArray()) {
			sb.append(p);
			sb.append(' ');
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
}
