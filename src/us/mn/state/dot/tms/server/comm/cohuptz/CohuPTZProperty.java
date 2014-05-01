/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
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

package us.mn.state.dot.tms.server.comm.cohuptz;

import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * Cohu PTZ Property
 *
 * @author Travis Swanston
 */
abstract public class CohuPTZProperty extends ControllerProperty {

	/**
	 * Calculate the XOR-based checksum of the given byte string.
	 *
	 * @param message The message for which to calculate the checksum.
	 * @param numBytes The number of message bytes to consider.
	 * @return The checksum for the message, or 0 on error.
	 *
	 * TODO: refactor returning Byte, to allow for null to indicate error.
	 */
	protected byte calculateChecksum(byte[] message, int numBytes) {
		if (message.length < 1) return 0;
		int lastIndex = (
			(numBytes <= message.length) ? (numBytes - 1)
			: (message.length - 1) );
		if (lastIndex < 0) return 0;

		byte runningXor = 0;
		for(int i = 1; i <= lastIndex; ++i) {
			runningXor ^= message[i];
			}
		return (byte) (
			((byte)(0x80)) + ((byte)(runningXor & (byte)0x0f)) );
	}

	/**
	 * Calculate the "preset byte" that corresponds to the given preset
	 * number.
	 * Presets [1..47] correspond to preset bytes [0x10..0x3e], and
	 * presets [48..64] correspond to preset bytes [0x60..0x70].
	 *
	 * @param presetNum The preset number, [1..64].
	 * @return The preset byte corresponding to the given preset number,
	 *         or null if the given preset number is invalid.
	 */
	protected Byte getPresetByte(int presetNum) {
		if (presetNum < 1) return null;
		if (presetNum > 64) return null;

		byte presetByte = 0x00;

		if (presetNum <= 47) {
			presetByte = (byte) (0x10 + (presetNum-1));
		}
		else {
			presetByte = (byte) (0x60 + (presetNum-1));
		}
		return Byte.valueOf(presetByte);
	}

	/**
	 * Calculate the pan/tilt "speed byte" that corresponds to the given
	 * speed value [-1..1].
	 *
	 * @param speed The speed value [-1..1].  Values outside this range
	 *              will be remapped.
	 * @return The pan/tilt speed byte [0x31..0x3f] corresponding to the
	 *         given speed value.  Note that 0x00 will not be returned, as
	 *         it seems to be a special, undocumented case (as of v6.8 of
	 *         the Cohu PTZ protocol specs) that appears to correspond to
	 *         some sort of "default" speed mode.
	 */
	protected byte getPTSpeedByte(float speed) {
		int range = 15;
		int scale = range - 1;

		speed = Math.abs(speed);
		float mapped = (speed * scale);
		int mapInt = Math.round(mapped);

		// sanity checks for floating point gotchas
		if (mapInt > scale)      mapInt = scale;
		if (mapInt < scale*(-1)) mapInt = scale*(-1);

		byte byteval = (byte) (0x31 + mapInt);
		return byteval;
	}

}
