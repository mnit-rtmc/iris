/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.g4;

/**
 * Vehicle class (size).
 *
 * @author Douglas Lau
 */
public enum VehicleClass {
	SMALL,		/* C0 */
	REGULAR,	/* C1 */
	MEDIUM,		/* C2 */
	LARGE,		/* C3 */
	TRUCK,		/* C4 */
	EXTRA_LARGE;	/* C5 */

	static public final int size = values().length;
	static public VehicleClass fromOrdinal(int o) {
		for(VehicleClass vc: VehicleClass.values()) {
			if(vc.ordinal() == o)
				return vc;
		}
		return SMALL;
	}
}
