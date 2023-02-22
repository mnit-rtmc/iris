/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2023  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.TagReaderSyncMode;
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

	/** Synchronization mode */
	private TagReaderSyncMode mode;

	/** Get the sync mode */
	public TagReaderSyncMode getMode() {
		return mode;
	}

	/** Set the sync mode */
	public void setMode(TagReaderSyncMode m) {
		mode = m;
	}

	/** Slave select count */
	private Integer slave;

	/** Get the slave select count */
	public Integer getSlaveSelectCount() {
		return slave;
	}

	/** Create a new master/slave property */
	public void setSlaveSelectCount(Integer s) {
		slave = s;
	}

	/** Create a new master/slave property */
	public MasterSlaveProp() {
		mode = null;
		slave = null;
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
		TagReaderSyncMode m = TagReaderSyncMode.fromBits(d[4]);
		if (m != null)
			mode = m;
		else
			throw new ParsingException("INVALID SYNC MODE");
		slave = (int) d[5];
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		TagReaderSyncMode m =
			(mode != null) ? mode : TagReaderSyncMode.SLAVE;
		int s = (slave != null) ? slave : 0;
		byte[] d = new byte[4];
		format16(d, 0, STORE);
		format8(d, 2, 1 << m.ordinal());
		format8(d, 3, s);
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
		return "master/slave: " + mode + ", " + slave;
	}
}
