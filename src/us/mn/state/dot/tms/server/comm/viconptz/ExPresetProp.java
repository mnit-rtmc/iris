/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.viconptz;

import java.io.IOException;

/**
 * Vicon property to recall or store an extended preset (greater than 15).
 * This command replaces PresetProperty for newer receivers.
 *
 * @author Douglas Lau
 */
public class ExPresetProp extends ExtendedProp {

	/** Special preset for camera reset (store) */
	static public final int SOFT_RESET = 97;

	/** Bit flags for param 1 recall preset function */
	static private final int FLAGS_RECALL = 0x10;

	/** Bit flags for param 1 store preset function */
	static private final int FLAGS_STORE = 0x11;

	/** Store (or recall) */
	private final boolean store;

	/** Preset to store or recall */
	private final int preset;

	/** Create a new extended preset property */
	public ExPresetProp(int d, boolean s, int p) throws IOException {
		super(d);
		store = s;
		preset = p;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "ex_preset: " + preset + " store:" + store;
	}

	/** Get command parameter 1 */
	@Override
	protected int getParam1() {
		return (presetFlags() << 7) | (preset & 0x7f);
	}

	/** Get parameter 1 preset flags */
	private int presetFlags() {
		return (store) ? FLAGS_STORE : FLAGS_RECALL;
	}

	/** Get command parameter 2 */
	@Override
	protected int getParam2() {
		return (panSpeed() << 7) | (tiltSpeed() & 0x7f);
	}

	/** Get preset pan speed */
	private int panSpeed() {
		return 0x7f;
	}

	/** Get preset tilt speed */
	private int tiltSpeed() {
		return 0x7f;
	}
}
