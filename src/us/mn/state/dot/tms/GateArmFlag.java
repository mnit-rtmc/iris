/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026  SRF Consulting Group
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
package us.mn.state.dot.tms;

import us.mn.state.dot.tms.utils.I18N;

/**
 * Gate arm status flags enumeration.
 *
 * Each value is a bit in the gate arm "statusFlags" attribute, in display
 * order.  At most one position flag is set, and only when the controller
 * reports an actual position.  The bit positions are independent of any
 * protocol; each comm protocol translates its own status codes to these
 * bits.
 *
 * @author John L. Stanley - SRF Consulting
 */
public enum GateArmFlag {
	GATE_FLAG_OVERRIDE, // 0 gate override in progress
	GATE_FLAG_CLOSED,   // 1 arm closed
	GATE_FLAG_OPENING,  // 2 arm opening
	GATE_FLAG_OPEN,     // 3 arm open
	GATE_FLAG_CLOSING,  // 4 arm closing
	GATE_FLAG_CLOSE_DELAY, // 5 arm open, waiting to close (close
	                       //   warning delay or gate override wait)
	GATE_FLAG_OTHER;    // 6 unknown status

	/** Get the bit for a flag */
	public int bit() {
		return 1 << ordinal();
	}

	/** Check if the flag bit is set */
	public boolean checkBit(int bits) {
		return (bits & bit()) != 0;
	}

	/** Get the bits for a set of flags */
	static public int toBits(GateArmFlag... flags) {
		int bits = 0;
		for (GateArmFlag f: flags)
			bits |= f.bit();
		return bits;
	}

	/** Bits for all arm position flags */
	static public final int POSITION_BITS = toBits(GATE_FLAG_CLOSED,
		GATE_FLAG_OPENING, GATE_FLAG_OPEN, GATE_FLAG_CLOSING);

	/** Get a string representation of the flag */
	@Override
	public String toString() {
		return I18N.get(name().toLowerCase().replace('_', '.'));
	}
}
