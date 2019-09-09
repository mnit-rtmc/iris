/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import us.mn.state.dot.tms.client.map.Marker;

/**
 * Marker used to paint warfinding purpose.
 *
 * @author Douglas Lau
 */
public class WayfindingMarker extends Marker {

	/** Create a new wayfinding marker */
	public WayfindingMarker() {
		super(16);
		float x = 38;
		float y = -22;
		path.moveTo(x + 14, y + 3);
		path.lineTo(x + 18, y + 3);
		path.lineTo(x + 18, y + 10);
		path.curveTo(x + 18, y + 16, x + 24, y + 21, x + 24, y + 21);
		path.lineTo(x + 26, y + 19);
		path.lineTo(x + 26, y + 26);
		path.lineTo(x + 19, y + 26);
		path.lineTo(x + 21, y + 24);
		path.curveTo(x + 21, y + 24, x + 16, y + 20, x + 16, y + 16);
		path.curveTo(x + 16, y + 20, x + 11, y + 24, x + 11, y + 24);
		path.lineTo(x + 13, y + 26);
		path.lineTo(x + 6, y + 26);
		path.lineTo(x + 6, y + 19);
		path.lineTo(x + 8, y + 21);
		path.curveTo(x + 8, y + 21, x + 14, y + 16, x + 14, y + 10);
		path.closePath();
	}
}
