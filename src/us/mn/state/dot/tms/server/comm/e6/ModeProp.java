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
 * Mode property.
 *
 * @author Douglas Lau
 */
public class ModeProp extends E6Property {

	/** Mode command */
	static private final Command CMD = new Command(CommandGroup.MODE);

	/** Store command code */
	static private final int STORE = 0x0001;

	/** Query command code */
	static private final int QUERY = 0x0002;

	/** Mode values */
	public enum Mode {
		stop(0),
		read_only(88),
		read_write(89);
		private Mode(int v) {
			value = v;
		}
		private final int value;
		static public Mode fromValue(int v) {
			for (Mode m: values())
				if (m.value == v)
					return m;
			return null;
		}
	};

	/** Mode value */
	private Mode mode;

	/** Get the mode */
	public Mode getMode() {
		return mode;
	}

	/** Create a mode property */
	public ModeProp(Mode m) {
		mode = m;
	}

	/** Create a mode property */
	public ModeProp() {
		this(Mode.stop);
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
		mode = Mode.fromValue(parse8(d, 4));
	}

	/** Get the store packet data */
	@Override
	public byte[] storeData() {
		byte[] d = new byte[3];
		format16(d, 0, STORE);
		format8(d, 2, mode.value);
		return d;
	}

	/** Parse a received store packet */
	@Override
	public void parseStore(byte[] d) throws IOException {
		if (d.length != 5)
			throw new ParsingException("DATA LEN: " + d.length);
		if (parse16(d, 2) != STORE)
			throw new ParsingException("SUB CMD");
		if (Mode.fromValue(parse8(d, 4)) != mode)
			throw new ParsingException("MODE");
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "mode: " + mode;
	}
}
