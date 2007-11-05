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
 * Marker used to paint roadway exit nodes.
 *
 * @author Douglas Lau
 */
public class ExitMarker extends AbstractMarker {

	/** Size (in user coordinates) to render marker */
	static protected final int MARKER_SIZE = 300;

	/** Create a new roadway exit node marker */
	public ExitMarker() {
		this(MARKER_SIZE);
	}

	/** Create a new roadway exit node marker */
	public ExitMarker(float size) {
		super(4);
		float half = size / 2;
		float third = size / 3;
		float sixth = size / 6;
		float offset = size / 4;
		path.moveTo(offset, 0);
		path.lineTo(offset, third);
		path.lineTo(offset + sixth, half);
		path.lineTo(offset + half, half);
		path.lineTo(offset, -half);
		path.lineTo(offset, 0);
		path.closePath();
		path.lineTo(0, 0);
	}
}
