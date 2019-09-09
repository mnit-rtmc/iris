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
 * Marker used to paint parking purpose.
 *
 * @author Douglas Lau
 */
public class ParkingMarker extends Marker {

	/** Create a new parking marker */
	public ParkingMarker() {
		super(16);
		float x = 38;
		float y = -22;
		path.moveTo(x + 11, y + 27);
		path.lineTo(x + 11, y + 5);
		path.lineTo(x + 14, y + 5);
		path.lineTo(x + 14, y + 15);
		path.curveTo(x + 18, y + 15, x + 22, y + 17, x + 22, y + 21);
		path.curveTo(x + 22, y + 25, x + 19, y + 27, x + 11, y + 27);
		path.closePath();
		path.moveTo(x + 14, y + 24);
		path.curveTo(x + 16, y + 24, x + 19, y + 23, x + 19, y + 21);
		path.curveTo(x + 19, y + 19, x + 16, y + 18, x + 14, y + 18);
		path.closePath();
	}
}
