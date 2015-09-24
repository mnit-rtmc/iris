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
 * E6 commands.
 *
 * @author Douglas Lau
 */
public class Command {

	/** Unsolicited command bit */
	static private final int UNSOLICITED_BIT = 0x0010;

	/** Acknowledge command bit */
	static private final int ACKNOWLEDGE_BIT = 0x0001;

	/** Create a command from bits.
	 * @param b Bits of command from packet.
	 * @return Valid command, or null on error. */
	static public Command create(int b) {
		CommandGroup cg = CommandGroup.lookup(b);
		if (cg != null) {
			int bits = cg.bits |
			          (b & UNSOLICITED_BIT) |
			          (b & ACKNOWLEDGE_BIT);
			if (bits == b) {
				boolean uns = (b & UNSOLICITED_BIT) != 0;
				boolean ack = (b & ACKNOWLEDGE_BIT) != 0;
				return new Command(cg, uns, ack);
			}
		}
		return null;
	}

	/** Command group */
	public final CommandGroup group;

	/** Flag for unsolicited */
	public final boolean unsolicited;

	/** Flag for acknowledge */
	public final boolean acknowledge;

	/** Create a new command */
	public Command(CommandGroup cg, boolean uns, boolean ack) {
		group = cg;
		unsolicited = uns;
		acknowledge = ack;
	}

	/** Create a new command */
	public Command(CommandGroup cg) {
		this(cg, false, false);
	}

	/** Get command bits */
	public int bits() {
		int b = group.bits;
		if (unsolicited)
			b |= UNSOLICITED_BIT;
		if (acknowledge)
			b |= ACKNOWLEDGE_BIT;
		return b;
	}

	/** Test for equality */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Command) {
			Command c = (Command) o;
			return group == c.group &&
			       unsolicited == c.unsolicited &&
			       acknowledge == c.acknowledge;
		} else
			return false;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(group);
		if (unsolicited)
 			sb.append(",unsolicited");
		if (acknowledge)
 			sb.append(",ack");
		return sb.toString();
	}
}
