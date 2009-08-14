/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import us.mn.state.dot.map.Style;

/**
 * A theme for drawing segment objects in a dull gray color.
 *
 * @author Douglas Lau
 */
public class FreewayTheme extends SegmentTheme {

	/** Create a new freeway theme */
	public FreewayTheme() {
		super("Freeway");
	}

	/** Get the style to draw a given segment */
	protected Style getStyle(MapSegment ms) {
		return DEFAULT_STYLE;
	}
}
