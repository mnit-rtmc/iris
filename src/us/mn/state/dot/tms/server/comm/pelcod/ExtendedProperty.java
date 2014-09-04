/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelcod;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An extended property is a command beyond the basic pan, tilt, zoom commands.
 *
 * @author Douglas Lau
 */
public class ExtendedProperty extends PelcoDProperty {

	/** Extended commands */
	static public enum Command {
		STORE_PRESET(3),	// 0000 0000 0000 0011
		CLEAR_PRESET(5),	// 0000 0000 0000 0101
		RECALL_PRESET(7);	// 0000 0000 0000 0111
		private Command(int b) {
			bits = b;
		}
		public int bits;
	}

	/** Extended command */
	private final Command cmd;

	/** Parameter 1 (stored in byte 5) */
	private final int param1;

	/** Parameter 2 (stored in byte 4) */
	private final int param2;

	/** Create a new extended property.
	 * @param c Extended command.
	 * @param p1 Extended parameter 1.
	 * @param p2 Extended parameter 2. */
	public ExtendedProperty(Command c, int p1, int p2) {
		cmd = c;
		param1 = p1;
		param2 = p2;
	}

	/** Create a new extended property.
	 * @param c Extended command.
	 * @param p1 Extended parameter 1. */
	public ExtendedProperty(Command c, int p1) {
		this(c, p1, 0);
	}

	/** Get the command bits (in the 2 LSBs) */
	@Override
	protected int getCommand() {
		return cmd.bits;
	}

	/** Get command parameter 1 */
	protected int getParam1() {
		return param1;
	}

	/** Get command parameter 2 */
	protected int getParam2() {
		return param2;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(OutputStream os, int drop) throws IOException {
		byte[] pkt = createPacket(drop);
		pkt[4] = (byte)getParam2();
		pkt[5] = (byte)getParam1();
		pkt[6] = calculateChecksum(pkt);
		os.write(pkt);
	}
}
