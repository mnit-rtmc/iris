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
 * Uplink source control property.
 *
 * @author Douglas Lau
 */
public class UplinkSourceProp extends E6Property {

	/** RF transceiver command */
	static private final Command CMD = new Command(
		CommandGroup.RF_TRANSCEIVER);

	/** Store command code */
	static private final int STORE = 0x57;

	/** Query command code */
	static private final int QUERY = 0x58;

	/** RF protocol */
	private final RFProtocol protocol;

	/** Source value */
	private Source source;

	/** Get the source (downlink/uplink) */
	public Source getValue() {
		return source;
	}

	/** Set the source (downlink/uplink)*/
	public void setValue(Source s) {
		source = s;
	}

	/** Create an uplink source control property */
	public UplinkSourceProp(RFProtocol p) {
		protocol = p;
		source = null;
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
		format8(d, 1, protocol.value << 4);
		format8(d, 2, 0x0D);	// Carriage-return
		return d;
	}

	/** Parse a received query packet */
	@Override
	public void parseQuery(byte[] d) throws IOException {
		if (d.length != 6)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse8(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		if (RFProtocol.fromValue(parse8(d, 3) >> 4) != protocol)
			throw new ParsingException("RF PROTOCOL");
		if (parse8(d, 4) != 0)
			throw new ParsingException("ACK");
		if (parse8(d, 5) != 0x0D)
			throw new ParsingException("CR");
		switch (parse8(d, 3) & 0x0F) {
			case 0:
				source = Source.uplink;
				break;
			case 1:
				source = Source.downlink;
				break;
			default:
				throw new ParsingException("SOURCE");
		}
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		byte[] d = new byte[3];
		format8(d, 0, STORE);
		format8(d, 1, protSrc());
		format8(d, 2, 0x0D);	// Carriage-return
		return d;
	}

	/** Get encoded protocol / source */
	private int protSrc() {
		// protocol is upper 4 bits, source is lower 4
		int prot = protocol.value << 4;
		if (Source.downlink == source)
			return prot | 1;
		else
			return prot;
	}

	/** Parse a received store packet */
	@Override
	public void parseStore(byte[] d) throws IOException {
		if (d.length != 6)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse8(d, 2) != STORE)
			throw new ParsingException("SUB CMD");
		if (parse8(d, 3) != protSrc())
			throw new ParsingException("PROT/SOURCE");
		if (parse8(d, 4) != 0) // 1 is NAK
			throw new ParsingException("ACK");
		if (parse8(d, 5) != 0x0D)
			throw new ParsingException("CR");
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return protocol + " uplink source control: " + source;
	}
}
