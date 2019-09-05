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
 * Marker used to paint tolling purpose.
 *
 * @author Douglas Lau
 */
public class TollingMarker extends Marker {

	/** Create a new tolling marker */
	public TollingMarker() {
		super(21);
		float x = 38;
		float y = -16;
		path.moveTo(x + 15, y + 3);
		path.lineTo(x + 5, y + 5);
		path.curveTo(x + 12, y + 5, x + 10, y + 7, x + 10, y + 10);
		path.curveTo(x + 10, y + 13, x + 12, y + 16, x + 15, y + 16);
		path.curveTo(x + 18, y + 16, x + 20, y + 18, x + 20, y + 21);
		path.curveTo(x + 20, y + 24, x + 18, y + 25, x + 16, y + 25);
		path.curveTo(x + 14, y + 25, x + 11, y + 24, x + 11, y + 22);
		path.lineTo(x + 9, y + 23);
		path.curveTo(x + 10, y + 26, x + 13, y + 27, x + 15, y + 27);
		path.lineTo(x + 15, y + 29);
		path.lineTo(x + 17, y + 29);
		path.lineTo(x + 17, y + 27);
		path.curveTo(x + 19, y + 27, x + 22, y + 26, x + 22, y + 21);
		path.curveTo(x + 22, y + 16, x + 18, y + 14, x + 16, y + 14);
		path.curveTo(x + 14, y + 14, x + 12, y + 13, x + 12, y + 10);
		path.curveTo(x + 12, y + 7, x + 14, y + 7, x + 16, y + 7);
		path.curveTo(x + 18, y + 7, x + 19, y + 8, x + 19, y + 9);
		path.lineTo(x + 21, y + 8);
		path.curveTo(x + 21, y + 7, x + 19, y + 5, x + 17, y + 5);
		path.lineTo(x + 17, y + 3);
		path.closePath();
	}
}
