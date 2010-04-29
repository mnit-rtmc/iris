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
package us.mn.state.dot.tms.server.comm.pelcod;

/**
 * This class creates a Pelco D request to instruct a camera to
 * store the current state in a preset location.
 *
 * @author Stephen Donecker
 */
public class StorePresetRequest extends PelcoDRequest {

	/** Requested preset to set */
	private final int m_preset;

	/** Create a new store preset command request */
	public StorePresetRequest(int preset) {
		m_preset = preset;
	}

	/** Format the request for the specified receiver address */
	public byte[] format(int drop) {
		byte[] message = new byte[7];
		message[0] = (byte)0xFF;
		message[1] = (byte)drop;
		message[2] = 0x0;
		message[3] = 0x3; // store preset
		message[4] = 0x0;
		message[5] = (byte)m_preset;
		message[6] = calculateChecksum(message);
		return message;
	}
}
