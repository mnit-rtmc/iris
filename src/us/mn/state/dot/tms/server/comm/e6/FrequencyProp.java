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
 * RF Frequency property.
 *
 * @author Douglas Lau
 */
public class FrequencyProp extends E6Property {

	/** RF transceiver command */
	static private final Command CMD = new Command(
		CommandGroup.RF_TRANSCEIVER);

	/** Store command code */
	static private final int STORE = 0x60;

	/** Query command code */
	static private final int QUERY = 0x61;

	/** Source values */
	public enum Source {
		downlink, uplink;
		static public Source fromOrdinal(int o) {
			for (Source s: values())
				if (s.ordinal() == o)
					return s;
			return null;
		}
	};

	/** Source value */
	private final Source source;

	/** Frequency value */
	private int value = 0;

	/** Create a frequency property */
	public FrequencyProp(Source s) {
		source = s;
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
		format8(d, 1, source.ordinal());
		format8(d, 2, 0x0D);	// Carriage-return
		return d;
	}

	/** Parse a received query packet */
	@Override
	public void parseQuery(byte[] d) throws IOException {
		if (d.length != 8)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse8(d, 2) != QUERY)
			throw new ParsingException("SUB CMD");
		if (parse8(d, 3) != source.ordinal())
			throw new ParsingException("SOURCE");
		if (parse8(d, 6) != 0)
			throw new ParsingException("ACK");
		if (parse8(d, 7) != 0x0D)
			throw new ParsingException("CR");
		value = parse16(d, 4);
	}

	/** Get a string representation */
	@Override
	public String toString() {
		float mhz = 800 + (value * 0.25f);
		return source + " frequency: " + mhz + " MHz";
	}
}
