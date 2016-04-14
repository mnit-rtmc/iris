/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import us.mn.state.dot.tms.client.map.Marker;

/**
 * Marker used to paint action plans.
 *
 * @author Douglas Lau
 */
public class PlanMarker extends Marker {

	/** Size in pixels to render marker */
	static protected final int MARKER_SIZE_PIX = 36;

	/** Create a new plan marker */
	public PlanMarker() {
		super(4);
		float size = MARKER_SIZE_PIX;
		float quarter = size / 4;
		float x = quarter;
		float y = quarter;
		path.moveTo(x, y);
		path.lineTo(x += quarter, y += quarter);
		path.lineTo(x -= quarter, y += quarter);
		path.lineTo(x -= quarter, y -= quarter);
		path.closePath();
	}
}
