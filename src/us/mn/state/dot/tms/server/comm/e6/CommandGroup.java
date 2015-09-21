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

/**
 * E6 command groups.
 *
 * @author Douglas Lau
 */
public enum CommandGroup {
	SYSTEM_INFO		(0x8000),
	DIGITAL_IO		(0x4000),
	RF_TRANSCEIVER		(0x2000),
	TAG_TRANSACTION_CONFIG	(0x1000),
	TAG_TRANSACTION		(0x0800),
	MODE			(0x0400),
	DIAGNOSTIC		(0x0200);

	/** Create a new command group */
	private CommandGroup(int b) {
		bits = b;
	}

	/** Bits for command group */
	public int bits;

	/** Get the bits for all command groups */
	static private int group_bits() {
		int b = 0;
		for (CommandGroup cg: values())
			b |= cg.bits;
		return b;
	}

	/** Lookup the command group for a command */
	static public CommandGroup lookup(int b) {
		int g_bits = b & group_bits();
		for (CommandGroup cg: values()) {
			if ((cg.bits & g_bits) == cg.bits)
				return cg;
		}
		return null;
	}
}
