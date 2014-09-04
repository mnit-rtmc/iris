/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
import java.io.OutputStream;

/**
 * Vicon property to recall or store a preset.
 *
 * @author Douglas Lau
 */
public class PresetProperty extends ViconPTZProperty {

	/** Store (or recall) */
	private final boolean store;

	/** Preset to store */
	private final int preset;

	/** Create a new preset property */
	public PresetProperty(boolean s, int p) {
		store = s;
		preset = p;
	}

	/** Get the preset bits */
	private byte presetBits() {
		return (byte)(recallOrStore() | (preset & 0x0f));
	}

	/** Get recall or store bit */
	private int recallOrStore() {
		return store ? 0x40 : 0x20;
	}

	/** Encode a preset request */
	@Override
	public void encodeStore(OutputStream os, int drop) throws IOException {
		byte[] pkt = new byte[6];
		pkt[0] = (byte)(0x80 | (drop >> 4));
		pkt[1] = (byte)((0x0f & drop) | CMD);
		pkt[2] = (byte)0x00; // pan/tilt functions
		pkt[3] = (byte)0x00; // lens functions
		pkt[4] = (byte)0x00; // aux functions
		pkt[5] = presetBits();
		os.write(pkt);
	}
}
