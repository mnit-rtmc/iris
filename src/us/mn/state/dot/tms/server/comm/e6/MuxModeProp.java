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
 * Antenna multiplexing mode property.
 *
 * @author Douglas Lau
 */
public class MuxModeProp extends E6Property {

	/** Digital IO command */
	static private final Command CMD = new Command(CommandGroup.DIGITAL_IO);

	/** Store command code */
	static private final int STORE = 0x0008;

	/** Query command code */
	static private final int QUERY = 0x0009;

	/** Mux mode values */
	public enum Value {
		no_multiplexing	(0x00),
		channel_0	(0x01),
		channel_0_1	(0x03),
		channel_2_3	(0x0C),
		channel_0_1_2	(0x07),
		channel_0_1_2_3	(0x0F);
		private Value(int b) {
			bits = b;
		}
		public final int bits;
		static public Value fromBits(int b) {
			for (Value v: values())
				if (v.bits == b)
					return v;
			return null;
		}
	};

	/** Mux mode value */
	private Value value;

	/** Create a mux mode property */
	public MuxModeProp(Value v) {
		value = v;
	}

	/** Create a mux mode property */
	public MuxModeProp() {
		this(Value.no_multiplexing);
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
		if (d.length != 5)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		Value v = Value.fromBits(d[4]);
		if (v != null)
			value = v;
		else
			throw new ParsingException("BAD MUX");
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		byte[] d = new byte[3];
		format16(d, 0, STORE);
		format8(d, 2, value.bits);
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
		return "mux mode: " + value;
	}
}
