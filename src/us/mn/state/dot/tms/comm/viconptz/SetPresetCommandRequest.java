/*
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
package us.mn.state.dot.tms.comm.viconptz;

/**
 * This class creates a Vicon request to instruct a camera to 
 * store the current state in a preset location.
 *
 * @author Stephen Donecker
 * @company University of California, Davis
 * @created July 2, 2008
 */
public class SetPresetCommandRequest extends Request {

	/** Requested preset to set */
	private final int m_preset;

	/** Create a new set preset command request */
	public SetPresetCommandRequest(int preset) {
		m_preset = preset;
	}

	/** Calculate the checksum of a message */
	private byte calculateChecksum(byte[] message) {
		int i;
		byte checksum = 0;
		for(i = 1; i < 6; i++)
			checksum += message[i];
		return checksum;
	}

	/** Format the request for the specified receiver address */
	public byte[] format(int drop) {
		byte[] message = new byte[7];
		message[0] = (byte)0xFF;
		message[1] = (byte)drop;
		message[2] = 0x0;
		message[3] = 0x3; // set preset 
		message[4] = 0x0;
		message[5] = (byte) m_preset; 
		message[6] = calculateChecksum(message);
		return message;
	}
}
