/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
 * Copyright (C) 2010  AHMCT, University of California
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
package us.mn.state.dot.tms.client.camera;

import us.mn.state.dot.tms.client.IrisMarker;

/**
 * Marker used to paint cameras.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class CameraMarker extends IrisMarker {

	/** Maximum size (in user coordinates) to render marker */
	static protected final int MARKER_SIZE_MAX = 600;

	/** Size in pixels to render marker */
	static protected final int MARKER_SIZE_PIX = 32;

	/** Create a new camera marker */
	public CameraMarker() {
		this(INIT_SCALE);
	}

	/** Create a new camera marker.
	 * @param scale Map scale in pixels per user coordinate. */
	public CameraMarker(float scale) {
		super(11, MARKER_SIZE_PIX, MARKER_SIZE_MAX);
		float size = getMarkerSize(scale);
		float tenth = size / 10;
		float quarter = size / 4;
		float third = size / 3;
		path.moveTo(0, third);
		path.lineTo(quarter, tenth);
		path.lineTo(third, tenth);
		path.lineTo(third, quarter);
		path.lineTo(size, quarter);
		path.lineTo(size, -quarter);
		path.lineTo(third, -quarter);
		path.lineTo(third, -tenth);
		path.lineTo(quarter, -tenth);
		path.lineTo(0, -third);
		path.closePath();
	}
}
