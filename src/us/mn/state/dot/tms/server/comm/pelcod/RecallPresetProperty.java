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
package us.mn.state.dot.tms.server.comm.pelcod;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class creates a Pelco D request to instruct the camera to recall
 * a preset state.
 *
 * @author Stephen Donecker
 * @author Douglas Lau
 */
public class RecallPresetProperty extends PelcoDProperty {

	/** Requested preset to set */
	private final int preset;

	/** Create a new recall preset property */
	public RecallPresetProperty(int p) {
		preset = p;
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(OutputStream os, int drop) throws IOException {
		byte[] pkt = createPacket(drop);
		pkt[2] = 0x0;
		pkt[3] = 0x7; // recall preset
		pkt[4] = 0x0;
		pkt[5] = (byte)preset;
		pkt[6] = calculateChecksum(pkt);
		os.write(pkt);
	}
}
