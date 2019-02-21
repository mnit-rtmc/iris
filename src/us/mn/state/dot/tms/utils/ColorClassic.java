/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import us.mn.state.dot.tms.DmsColor;

/**
 * Enumeration of "classic" MULTI color scheme values.
 *
 * @author Douglas Lau
 */
public enum ColorClassic {
	black(DmsColor.BLACK),     // 0
	red(DmsColor.RED),         // 1
	yellow(DmsColor.YELLOW),   // 2
	green(DmsColor.GREEN),     // 3
	cyan(DmsColor.CYAN),       // 4
	blue(DmsColor.BLUE),       // 5
	magenta(DmsColor.MAGENTA), // 6
	white(DmsColor.WHITE),     // 7
	orange(DmsColor.ORANGE),   // 8
	amber(DmsColor.AMBER);     // 9

	/** Create a new classic color */
	private ColorClassic(DmsColor c) {
		clr = c;
	}

	/** DMS color value */
	public final DmsColor clr;

	/** Enumerated values */
	static private final ColorClassic[] VALUES = values();

	/** Get classic color from an ordinal value */
	static public ColorClassic fromOrdinal(int o) {
		if (o >= 0 && o < VALUES.length)
			return VALUES[o];
		else
			return null;
	}

	/** Get classic color from a DMS color */
	static public ColorClassic fromColor(DmsColor clr) {
		for (ColorClassic c : VALUES) {
			if (c.clr == clr)
				return c;
		}
		return null;
	}
}
