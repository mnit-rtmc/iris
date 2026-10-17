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
 * Gate arm error flags enumeration.
 *
 * Each value is a bit in the gate arm "errorFlags" attribute.  Values are
 * ordered from most to least serious, which is also the display order.
 * The bit positions are independent of any protocol; each comm protocol
 * translates its own error codes to these bits.
 *
 * @author John L. Stanley - SRF Consulting
 */
public enum GateArmError {
	GATE_ERROR_COMM,         //  0 no valid response from controller
	GATE_ERROR_STILL_CLOSED, //  1 timed out, never moved from closed
	GATE_ERROR_OPEN_FAILED,  //  2 timed out while opening
	GATE_ERROR_STILL_OPEN,   //  3 timed out, never moved from open
	GATE_ERROR_CLOSE_FAILED, //  4 timed out while closing
	GATE_ERROR_CONFIG,       //  5 gate arm not configured
	GATE_ERROR_MODBUS,       //  6 MODBUS error
	GATE_ERROR_LIGHTS,       //  7 arm lights error
	GATE_ERROR_SIGN,         //  8 warning sign error
	GATE_ERROR_OTHER;        //  9 unknown error

	/** Get the bit for an error */
	public int bit() {
		return 1 << ordinal();
	}

	/** Check if the error bit is set */
	public boolean checkBit(int bits) {
		return (bits & bit()) != 0;
	}

	/** Get the bits for a set of errors */
	static public int toBits(GateArmError... errors) {
		int bits = 0;
		for (GateArmError e: errors)
			bits |= e.bit();
		return bits;
	}

	/** Get a string representation of the error */
	@Override
	public String toString() {
		return I18N.get(name().toLowerCase().replace('_', '.'));
	}
}
