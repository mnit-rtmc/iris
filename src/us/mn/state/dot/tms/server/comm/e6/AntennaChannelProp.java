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
 * Manual antenna channel control property.
 *
 * @author Douglas Lau
 */
public class AntennaChannelProp extends E6Property {

	/** Tag transaction config command */
	static private final Command CMD = new Command(
		CommandGroup.TAG_TRANSACTION_CONFIG);

	/** Store command code */
	static private final int STORE = 0x002A;

	/** Query command code */
	static private final int QUERY = 0x002B;

	/** Channel control values */
	public enum Value {
		channel_0(1),
		channel_1(2),
		channel_2(3),
		channel_3(4),
		disable_manual_control(5);
		private Value(int i) {
			value = i;
		}
		private final int value;
		static public Value fromValue(int i) {
			for (Value v: values())
				if (v.value == i)
					return v;
			return null;
		}
	};

	/** Antenna channel control value */
	private Value value;

	/** Create an antenna channel property */
	public AntennaChannelProp(Value v) {
		value = v;
	}

	/** Create an antenna channel property */
	public AntennaChannelProp() {
		this(Value.disable_manual_control);
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
		Value v = Value.fromValue(d[4]);
		if (v != null)
			value = v;
		else
			throw new ParsingException("BAD ANTENNA CHANNEL");
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		byte[] d = new byte[3];
		format16(d, 0, STORE);
		format8(d, 2, value.value);
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
		return "antenna channel: " + value;
	}
}
