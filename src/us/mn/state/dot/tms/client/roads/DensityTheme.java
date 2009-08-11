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

import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.Style;

/**
 * A theme for drawing segment objects based on density thresholds.
 *
 * @author Douglas Lau
 */
public class DensityTheme extends SegmentTheme {

	/** Density styles */
	static protected final Style[] D_STYLES = new Style[] {
		new Style("0-29 veh/mi", GREEN),
		new Style("30-49 veh/mi", ORANGE),
		new Style("50+ veh/mi", RED),
		new Style("Crazy data", VIOLET)
	};

	/** Create a new density theme */
	public DensityTheme() {
		super("Density");
		for(Style s: D_STYLES)
			addStyle(s);
	}

	/** Get the style to draw a given segment */
	protected Style getStyle(Segment s) {
		// FIXME
		return D_STYLES[1];
	}
}
