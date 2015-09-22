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
 * RF attenuation property.  NOTE: if this property is queried before being
 * stored, the response will be a NAK, as a SUB_COMMAND_ERROR.
 *
 * @author Douglas Lau
 */
public class RFAttenProp extends E6Property {

	/** RF transceiver command */
	static private final Command CMD = new Command(
		CommandGroup.RF_TRANSCEIVER);

	/** Store command code */
	static private final int STORE = 0x51;

	/** Query command code */
	static private final int QUERY = 0x52;

	/** RF protocol */
	private final RFProtocol protocol;

	/** Downlink attenuation value (0 - 15 dB) */
	private int downlink;

	/** Uplink attenuation value (0 - 15 dB) */
	private int uplink;

	/** Create an FR attenuation property */
	public RFAttenProp(RFProtocol p, int d, int u) {
		protocol = p;
		downlink = d;
		uplink = u;
	}

	/** Create an FR attenuation property */
	public RFAttenProp(RFProtocol p) {
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
		downlink = (d[4] >> 4) & 0x0F;
		uplink =   (d[4] >> 0) & 0x0F;
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		byte[] d = new byte[4];
		format8(d, 0, STORE);
		format8(d, 1, protocol.ordinal() << 4);
		format8(d, 2, ((downlink << 4) & 0xF0) | (uplink & 0x0F));
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
		return protocol + " RF attenuation: " + downlink +
			" dB (downlink), " + uplink + " dB (uplink)";
	}
}
