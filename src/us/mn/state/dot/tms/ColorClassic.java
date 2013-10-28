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
package us.mn.state.dot.tms;

/**
 * Enumeration of "classic" MULTI color scheme values.
 *
 * @author Douglas Lau
 */
public enum ColorClassic {
	black(DmsColor.BLACK),
	red(DmsColor.RED),
	yellow(DmsColor.YELLOW),
	green(DmsColor.GREEN),
	cyan(DmsColor.CYAN),
	blue(DmsColor.BLUE),
	magenta(DmsColor.MAGENTA),
	white(DmsColor.WHITE),
	orange(DmsColor.ORANGE),
	amber(DmsColor.AMBER);

	/** Create a new classic color */
	private ColorClassic(DmsColor c) {
		clr = c;
	}

	/** DMS color value */
	public DmsColor clr;

	/** Get classic color from an ordinal value */
	static public ColorClassic fromOrdinal(int o) {
		for(ColorClassic c: values()) {
			if(c.ordinal() == o)
				return c;
		}
		return null;
	}
}
