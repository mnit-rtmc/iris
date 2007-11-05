/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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

import us.mn.state.dot.map.marker.AbstractMarker;

/**
 * Marker used to paint roadway access nodes.
 *
 * @author Douglas Lau
 */
public class AccessMarker extends AbstractMarker {

	/** Size (in user coordinates) to render marker */
	static protected final int MARKER_SIZE = 300;

	/** Create a new access marker */
	public AccessMarker() {
		this(MARKER_SIZE);
	}

	/** Create a new access marker */
	public AccessMarker(float size) {
		super(5);
		float half = size / 2;
		float third = size / 3;
		path.moveTo(0, half);
		path.lineTo(third, 0);
		path.lineTo(0, -half);
		path.lineTo(-third, 0);
		path.closePath();
	}
}
