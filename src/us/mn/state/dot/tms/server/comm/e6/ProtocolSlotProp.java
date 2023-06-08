/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  Minnesota Department of Transportation
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
 * Protocol slot property.
 *
 * This is only required for the IAG protocol.
 *
 * @author Douglas Lau
 */
public class ProtocolSlotProp extends E6Property {

	/** Tag transaction config command */
	static private final Command CMD = new Command(
		CommandGroup.TAG_TRANSACTION_CONFIG);

	/** Store command code */
	static private final int STORE = 0x004B;

	/** Query command code */
	static private final int QUERY = 0x004C;

	/** RF protocol */
	public final RFProtocol protocol;

	/** Protocol slot number */
	private Integer slot;

	/** Get the slot number */
	public Integer getSlot() {
		return slot;
	}

	/** Set the slot number */
	public void setSlot(Integer sl) {
		slot = sl;
	}

	/** Create a protocol slot property */
	public ProtocolSlotProp(RFProtocol p) {
		protocol = p;
		slot = null;
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
		format8(d, 2, protocol.value);
		return d;
	}

	/** Parse a received query packet */
	@Override
	public void parseQuery(byte[] d) throws IOException {
		if (d.length != 6)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		if (RFProtocol.fromValue(parse8(d, 4)) != protocol)
			throw new ParsingException("RF PROTOCOL");
		slot = parse8(d, 5);
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		int sl = (slot != null) ? slot : 0;
		byte[] d = new byte[4];
		format16(d, 0, STORE);
		format8(d, 2, protocol.value);
		format8(d, 3, sl);
		return d;
	}

	/** Parse a received store packet */
	@Override
	public void parseStore(byte[] d) throws IOException {
		// FIXME: is protocol included in response? (doc says no)
		if (d.length != 4 && d.length != 5)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != STORE)
			throw new ParsingException("SUB CMD");
		if (d.length == 5 &&
		    RFProtocol.fromValue(parse8(d, 4)) != protocol)
			throw new ParsingException("RF PROTOCOL");
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return protocol + " slot: " + slot;
	}
}
