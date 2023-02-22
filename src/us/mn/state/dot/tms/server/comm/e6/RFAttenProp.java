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
	private Integer downlink;

	/** Get downlink attenuation (dB) */
	public Integer getDownlinkDb() {
		return downlink;
	}

	/** Set downlink attenuation (dB) */
	public void setDownlinkDb(Integer d) {
		downlink = d;
	}

	/** Uplink attenuation value (0 - 15 dB) */
	private Integer uplink;

	/** Get uplink attenuation (dB) */
	public Integer getUplinkDb() {
		return uplink;
	}

	/** Set uplink attenuation (dB) */
	public void setUplinkDb(Integer u) {
		uplink = u;
	}

	/** Create an FR attenuation property */
	public RFAttenProp(RFProtocol p) {
		protocol = p;
		downlink = null;
		uplink = null;
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
		if (d.length != 7)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse8(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		if (RFProtocol.fromValue(parse8(d, 3) >> 4) != protocol)
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
		int dn = (downlink != null) ? downlink : 0;
		int up = (uplink != null) ? uplink : 0;
		byte[] d = new byte[4];
		format8(d, 0, STORE);
		format8(d, 1, protocol.value << 4);
		format8(d, 2, ((dn << 4) & 0xF0) | (up & 0x0F));
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
		if (RFProtocol.fromValue(parse8(d, 3) >> 4) != protocol)
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
