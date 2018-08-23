/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
 * Switch buffered tag transaction mode property.
 *
 * @author Douglas Lau
 */
public class SwitchBufferedTagTransactionModeProp extends E6Property {

	/** System information command */
	static private final Command CMD =new Command(CommandGroup.SYSTEM_INFO);

	/** Store command code */
	static private final int STORE = 0x001A;

	/** Query command code */
	static private final int QUERY = 0x001B;

	/** Buffering enabled */
	private boolean enabled;

	/** Is buffering enabled? */
	public boolean isEnabled() {
		return enabled;
	}

	/** Create a new switch buffered tag transaction mode property */
	public SwitchBufferedTagTransactionModeProp(boolean e) {
		enabled = e;
	}

	/** Create a new switch buffered tag transaction mode property */
	public SwitchBufferedTagTransactionModeProp() {
		this(false);
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
		enabled = parse8(d, 4) != 0;
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		byte[] d = new byte[3];
		format16(d, 0, STORE);
		format8(d, 2, (enabled) ? 1 : 0);
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
		return "switch buffered tag transaction mode: " + enabled;
	}
}
