/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026  Minnesota Department of Transportation
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

/**
 * Gate Arm control mode enumeration.
 *
 * The ordinal values correspond to the records in the
 * iris.gate_arm_control_mode look-up table.
 *
 * @author Douglas Lau
 */
public enum GateArmControlMode {

	/** Gate Arm control modes */
	NORMAL,      /* 0: normal control */
	OVERRIDE,    /* 1: local override */
	CLOSE_DELAY; /* 2: close delay */

	/** Get gate arm control mode from an ordinal value */
	static public GateArmControlMode fromOrdinal(int o) {
		return (o >= 0 && o < values().length) ? values()[o] : NORMAL;
	}
}
