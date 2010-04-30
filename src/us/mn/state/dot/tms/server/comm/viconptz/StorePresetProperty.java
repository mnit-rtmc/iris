/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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

/**
 * Vicon property to store the current state in a preset location.
 *
 * @author Douglas Lau
 */
public class StorePresetProperty extends ViconPTZProperty {

	/** Preset to store */
	protected final int preset;

	/** Create a new store preset property */
	public StorePresetProperty(int p) {
		preset = p;
	}

	/** Format for the specified receiver address */
	public byte[] format(int drop) {
		byte[] message = new byte[6];
		message[0] = (byte)(0x80 | (drop >> 4));
		message[1] = (byte)((0x0f & drop) | CMD);
		message[2] = (byte)0x00; // pan/tilt functions
		message[3] = (byte)0x00; // lens functions
		message[4] = (byte)0x00; // aux functions
		message[5] = (byte)(0x40 | (preset & 0x0f));
		return message;
	}
}
