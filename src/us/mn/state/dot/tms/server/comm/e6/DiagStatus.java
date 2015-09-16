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

/**
 * Diagnostic status property.
 *
 * @author Douglas Lau
 */
public class DiagStatus extends E6Property {

	/** Diagnostic command */
	static private final Command CMD = new Command(CommandGroup.DIAGNOSTIC,
		false, false);

	/** Command code */
	static private final int code = 0x0001;

	/** Get the query command */
	@Override
	public Command queryCmd() {
		return CMD;
	}

	/** Get the packet data */
	@Override
	public byte[] data() {
		byte[] d = new byte[2];
		d[0] = (byte) (code >> 8);
		d[1] = (byte) (code >> 0);
		return d;
	}

	/** Parse a received packet */
	public void parse(byte[] data) throws IOException {
		// FIXME
	}
}
