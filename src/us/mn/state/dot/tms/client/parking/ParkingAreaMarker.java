/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.parking;

import us.mn.state.dot.tms.client.map.Marker;

/**
 * Marker used to paint parking areas.
 *
 * @author Douglas Lau
 */
public class ParkingAreaMarker extends Marker {

	/** Size in pixels to render marker */
	static protected final int MARKER_SIZE_PIX = 20;

	/** Create a new parking area marker */
	public ParkingAreaMarker() {
		super(8);
		float size = MARKER_SIZE_PIX;
		float half = size / 2;
		float quarter = size / 4;
		float x = 0;
		float y = 0;
		path.moveTo(x += quarter, y);
		path.lineTo(x += half, y += quarter);
		path.lineTo(x, y += size - quarter);
		path.lineTo(x -= half, y -= quarter);
		path.moveTo(x, y -= quarter);
		path.lineTo(x += half, y += quarter);
		path.moveTo(x -= half, y -= half);
		path.lineTo(x += half, y += quarter);
	}
}
