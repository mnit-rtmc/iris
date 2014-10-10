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

/**
 * An extended property is a command beyond the basic pan, tilt, zoom commands.
 *
 * @author Douglas Lau
 */
public class ExtendedProperty extends PelcoDProperty {

	/** Bit flag for extended function */
	static private final int EXTENDED = 1 << 0;

	/** Extended commands */
	static public enum Command {
		STORE_PRESET(3),		// 0000 0011
		CLEAR_PRESET(5),		// 0000 0101
		RECALL_PRESET(7),		// 0000 0111
		SET_AUX(9),			// 0000 1001
		CLEAR_AUX(0x0B),		// 0000 1011
		REMOTE_RESET(0x0F),		// 0000 1111
		SET_ZONE_START(0x11),		// 0001 0001
		SET_ZONE_END(0x13),		// 0001 0011
		WRITE_CHAR(0x15),		// 0001 0101
		CLEAR_CHARS(0x17),		// 0001 0111
		ACK_ALARM(0x19),		// 0001 1001
		ZONE_SCAN_ON(0x1B),		// 0001 1011
		ZONE_SCAN_OFF(0x1D),		// 0001 1101
		SET_PATTERN_START(0x1F),	// 0001 1111
		SET_PATTERN_STOP(0x21),		// 0010 0001
		RUN_PATTERN(0x23),		// 0010 0011
		SET_ZOOM_SPEED(0x25),		// 0010 0101
		SET_FOCUS_SPEED(0x27),		// 0010 0111
		RESET_TO_DEFAULTS(0x29),	// 0010 1001
		AUTO_FOCUS(0x2B),		// 0010 1011
		AUTO_IRIS(0x2D);		// 0010 1101
		private Command(int b) {
			bits = b | EXTENDED;
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

	/** Create a new extended property.
	 * @param c Extended command. */
	public ExtendedProperty(Command c) {
		this(c, 0, 0);
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "ext cmd: " + cmd + " p1:" + param1 + " p2:" + param2;
	}

	/** Get the command bits (in the 2 LSBs) */
	@Override
	protected int getCommand() {
		return cmd.bits;
	}

	/** Get command parameter 1 */
	@Override
	protected int getParam1() {
		return param1;
	}

	/** Get command parameter 2 */
	@Override
	protected int getParam2() {
		return param2;
	}
}
