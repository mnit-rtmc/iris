/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
 * Copyright (C) 2016  Minnesota Department of Transportation
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

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.InvalidAddressException;

/**
 * Cohu PTZ Property
 *
 * @author Travis Swanston
 */
abstract public class CohuPTZProperty extends ControllerProperty {

	/**
	 * Absolute value of PTZ movement threshold.
	 * PTZ vectors below this value will be considered as stop commands.
	 */
	static protected final float PTZ_THRESH = 0.001F;

	/** Cohu camera address range constants */
	static private final int ADDR_MIN = 1;
	static private final int ADDR_MAX = 223;

	/** Check drop address validity */
	static private boolean isAddressValid(int drop) {
		return ((drop >= ADDR_MIN) && (drop <= ADDR_MAX));
	}

	/** Create a Cohu packet */
	protected byte[] createPacket(int drop) throws IOException {
		if (!isAddressValid(drop))
			throw new InvalidAddressException(drop);
		return new byte[5];
	}

	/**
	 * Calculate the XOR-based checksum of the given Cohu message.
	 *
	 * @param message The message for which to calculate the checksum.
	 * @param first The index of the first byte in the checksum range.
	 * @param last The index of the last byte in the checksum range.
	 * @return A Byte representing the checksum for the message,
	 *         or null on error.
	 */
	protected Byte calculateChecksum(byte[] message, int first, int last) {
		if (message.length < 1) return null;
		if (first < 0) return null;
		if (last < 0) return null;
		if (first > last) return null;
		if (last >= message.length) return null;

		byte runningXor = 0;
		for (int i = first; i <= last; ++i)
			runningXor ^= message[i];
		return (byte) (0x80 + ((runningXor & (byte) 0x0f)));
	}

	/**
	 * Calculate the "preset byte" that corresponds to the given preset
	 * number.
	 * Presets [1..47] correspond to preset bytes [0x10..0x3e], and
	 * presets [48..64] correspond to preset bytes [0x60..0x70].
	 *
	 * @param p The preset number, [1..64].
	 * @return The preset byte corresponding to the given preset number,
	 *         or null if the given preset number is invalid.
	 */
	protected Byte getPresetByte(int p) {
		if (p < 1 || p > 64)
			return null;
		else {
			return (p <= 47)
			      ? (byte) (0x10 + (p - 1))
			      : (byte) (0x60 + (p - 1));
		}
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
	protected byte getPanTiltSpeedByte(float speed) {
		int range = (0x3f - 0x31) + 1;		// excludes 0x00
		int scale = range - 1;

		speed = Math.abs(speed);
		float mapped = (speed * scale);
		int mapInt = Math.round(mapped);

		// sanity check for floating point gotchas
		if (mapInt > scale)
			mapInt = scale;

		return (byte) (0x31 + mapInt);
	}

	/**
	 * Calculate the zoom "speed byte" that corresponds to the given
	 * speed value [-1..1].
	 *
	 * @param speed The speed value [-1..1].  Values outside this range
	 *              will be remapped.
	 * @return The zoom speed byte [0x30..0x32] corresponding to the
	 *         given speed value.
	 */
	protected byte getZoomSpeedByte(float speed) {
		int range = (0x32 - 0x30) + 1;
		int scale = range - 1;

		speed = Math.abs(speed);
		float mapped = (speed * scale);
		int mapInt = Math.round(mapped);

		// sanity check for floating point gotchas
		if (mapInt > scale)
			mapInt = scale;

		return (byte) (0x30 + mapInt);
	}
}
