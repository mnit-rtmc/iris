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
		float y = -22;
		path.moveTo(x + 15, y + 29);
		path.lineTo(x + 15, y + 26);
		path.curveTo(x + 12, y + 26, x + 9, y + 24, x + 9, y + 21);
		path.curveTo(x + 9, y + 18, x + 12, y + 15, x + 15, y + 15);
		path.curveTo(x + 18, y + 15, x + 20, y + 14, x + 20, y + 12);
		path.curveTo(x + 20, y + 10, x + 18, y + 9, x + 16, y + 9);
		path.curveTo(x + 14, y + 9, x + 11, y + 10, x + 11, y + 12);
		path.lineTo(x + 9, y + 10);
		path.curveTo(x + 10, y + 7, x + 13, y + 6, x + 15, y + 6);
		path.lineTo(x + 15, y + 3);
		path.lineTo(x + 17, y + 3);
		path.lineTo(x + 17, y + 6);
		path.curveTo(x + 19, y + 6, x + 23, y + 7, x + 23, y + 12);
		path.curveTo(x + 22, y + 17, x + 17, y + 18, x + 15, y + 18);
		path.curveTo(x + 13, y + 18, x + 12, y + 19, x + 12, y + 21);
		path.curveTo(x + 12, y + 23, x + 15, y + 23, x + 16, y + 23);
		path.curveTo(x + 17, y + 23, x + 19, y + 22, x + 19, y + 21);
		path.lineTo(x + 21, y + 23);
		path.curveTo(x + 21, y + 24, x + 19, y + 26, x + 17, y + 26);
		path.lineTo(x + 17, y + 29);
		path.closePath();
	}
}
