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
 * Master / slave reader property.
 *
 * @author Douglas Lau
 */
public class MasterSlaveProp extends E6Property {

	/** Tag transaction config command */
	static private final Command CMD = new Command(
		CommandGroup.TAG_TRANSACTION_CONFIG);

	/** Store command code */
	static private final int STORE = 0x0045;

	/** Query command code */
	static private final int QUERY = 0x0046;

	/** Master / slave values */
	public enum Value {
		slave, master, gps_secondary, gps_primary;
		static public Value fromBits(int b) {
			for (Value v: values()) {
				if ((1 << v.ordinal()) == b)
					return v;
			}
			return null;
		}
	};

	/** Master / slave value */
	private Value value;

	/** Slave select count */
	private int slave;

	/** Create a new master/slave property */
	public MasterSlaveProp(Value v, int s) {
		value = v;
		slave = s;
	}

	/** Create a new master/slave property */
	public MasterSlaveProp() {
		this(Value.slave, 0);
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
		Value v = Value.fromBits(d[4]);
		if (v != null)
			value = v;
		else
			throw new ParsingException("BAD MASTER/SLAVE");
		slave = d[5];
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		byte[] d = new byte[4];
		format16(d, 0, STORE);
		format8(d, 2, 1 << value.ordinal());
		format8(d, 3, slave);
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
		return "master/slave: " + value + ", " + slave;
	}
}
