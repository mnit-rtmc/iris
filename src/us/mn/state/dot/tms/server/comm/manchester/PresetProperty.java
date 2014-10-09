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
package us.mn.state.dot.tms.server.comm.manchester;

/**
 * Manchester property to recall or store a preset.
 *
 * @author Douglas Lau
 */
public class PresetProperty extends ManchesterProperty {

	/** Store (or recall) */
	private final boolean store;

	/** Preset to store or recall */
	private final int preset;

	/** Create a new preset property */
	public PresetProperty(boolean s, int p) {
		store = s;
		preset = p;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "preset: " + preset + " store:" + store;
	}

	/** Get command bits */
	@Override
	protected byte commandBits() {
		return (byte)(recallOrStore() | presetBits());
	}

	/** Get recall or store bit */
	private byte recallOrStore() {
		return store ? EX_STORE_PRESET : EX_RECALL_PRESET;
	}

	/** Get preset bits */
	private byte presetBits() {
		return (byte)((preset << 1) & 0x0E);
	}

	/** Check if packet is extended function */
	@Override
	protected boolean isExtended() {
		return true;
	}
}
