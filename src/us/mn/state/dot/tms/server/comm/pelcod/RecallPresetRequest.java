/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
 * This class creates a Pelco D request to instruct the camera to recall
 * a preset state.
 *
 * @author Stephen Donecker
 * @company University of California, Davis
 */
public class RecallPresetRequest extends Request {

	/** Requested preset to set */
	private final int m_preset;

	/** Create a new recall preset request */
	public RecallPresetRequest(int preset) {
		m_preset = preset;
	}

	/** Format the request for the specified receiver address */
	public byte[] format(int drop) {
		byte[] message = new byte[7];
		message[0] = (byte)0xFF;
		message[1] = (byte)drop;
		message[2] = 0x0;
		message[3] = 0x7; // recall preset
		message[4] = 0x0;
		message[5] = (byte)m_preset;
		message[6] = calculateChecksum(message);
		return message;
	}
}
