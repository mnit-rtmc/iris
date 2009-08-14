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
 * A theme for drawing segment objects based on flow thresholds.
 *
 * @author Douglas Lau
 */
public class FlowTheme extends SegmentTheme {

	/** Flow styles */
	static protected final Style[] F_STYLES = new Style[] {
		new Style("0-1500 veh/hr", GREEN),
		new Style("1501-2000 veh/hr", YELLOW),
		new Style("2001-2500 veh/hr", ORANGE),
		new Style("2501-4000 veh/hr", RED),
		new Style("Crazy data", VIOLET)
	};

	/** Create a new flow theme */
	public FlowTheme() {
		super("Flow");
		for(Style s: F_STYLES)
			addStyle(s);
	}

	/** Get the style to draw a given segment */
	protected Style getStyle(MapSegment ms) {
		Integer f = ms.getFlow();
		if(f == null)
			return DEFAULT_STYLE;
		if(f <= 1500)
			return F_STYLES[0];
		if(f <= 2000)
			return F_STYLES[1];
		if(f <= 2500)
			return F_STYLES[2];
		if(f <= 4000)
			return F_STYLES[3];
		return F_STYLES[4];
	}
}
