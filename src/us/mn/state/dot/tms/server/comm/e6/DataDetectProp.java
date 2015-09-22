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
 * Data detect threshold property.
 * For SeGo, the range is 0 - 20 dB.
 * For ASTMv6, the range is 0 - 15 dB.
 *
 * @author Douglas Lau
 */
public class DataDetectProp extends E6Property {

	/** RF transceiver command */
	static private final Command CMD = new Command(
		CommandGroup.RF_TRANSCEIVER);

	/** Store command code */
	static private final int STORE = 0x53;

	/** Query command code */
	static private final int QUERY = 0x54;

	/** RF protocol */
	private final RFProtocol protocol;

	/** Data detect value (0 - 20 dB) */
	private int value;

	/** Create a data detect property */
	public DataDetectProp(RFProtocol p, int v) {
		protocol = p;
		value = v;
	}

	/** Create a data detect property */
	public DataDetectProp(RFProtocol p) {
		this(p, 0);
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
		format8(d, 0, QUERY);
		format8(d, 1, protocol.ordinal() << 4);
		format8(d, 2, 0x0D);	// Carriage-return
		return d;
	}

	/** Parse a received query packet */
	@Override
	public void parseQuery(byte[] d) throws IOException {
		if (d.length != 7)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse8(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		if (RFProtocol.fromOrdinal(parse8(d, 3) >> 4) != protocol)
			throw new ParsingException("RF PROTOCOL");
		if (parse8(d, 5) != 0)
			throw new ParsingException("ACK");
		if (parse8(d, 6) != 0x0D)
			throw new ParsingException("CR");
		value = parse8(d, 4);
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		byte[] d = new byte[4];
		format8(d, 0, STORE);
		format8(d, 1, protocol.ordinal() << 4);
		format8(d, 2, value);
		format8(d, 3, 0x0D);	// Carriage-return
		return d;
	}

	/** Parse a received store packet */
	@Override
	public void parseStore(byte[] d) throws IOException {
		if (d.length != 6)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse8(d, 2) != STORE)
			throw new ParsingException("SUB CMD");
		if (RFProtocol.fromOrdinal(parse8(d, 3) >> 4) != protocol)
			throw new ParsingException("RF PROTOCOL");
		if (parse8(d, 4) != 0)
			throw new ParsingException("ACK");
		if (parse8(d, 5) != 0x0D)
			throw new ParsingException("CR");
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return protocol + " data detect: " + value + " dB";
	}
}
