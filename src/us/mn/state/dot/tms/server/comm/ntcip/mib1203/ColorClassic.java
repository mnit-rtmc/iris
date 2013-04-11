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
package us.mn.state.dot.tms.server.comm.ntcip.mib1203;

/**
 * Enumeration of "classic" (v1) color scheme values.
 *
 * @author Douglas Lau
 */
public enum ColorClassic {
	black, red, yellow, green, cyan, blue, magenta, white, orange, amber;

	/** Get classic color from an ordinal value */
	static public ColorClassic fromOrdinal(int o) {
		for(ColorClassic c: values()) {
			if(c.ordinal() == o)
				return c;
		}
		return null;
	}
}
