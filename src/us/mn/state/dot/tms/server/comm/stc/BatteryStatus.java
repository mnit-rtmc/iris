/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.stc;

/**
 * Battery status for STC gate arms.
 *
 * @author Douglas Lau
 */
public enum BatteryStatus {
	DEAD,				/*  0 */
	DEAD_OPEN_GATE,			/*  1 */
	CONSERVE_LEVEL_2,		/*  2 */
	CONSERVE_LEVEL_1,		/*  3 */
	OK;				/*  4 */

	/** Lookup a battery status from ordinal */
	static public BatteryStatus fromOrdinal(int o) {
		for(BatteryStatus bs: BatteryStatus.values()) {
			if(bs.ordinal() == o)
				return bs;
		}
		return null;
	}
}
