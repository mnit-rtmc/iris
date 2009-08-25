/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.awt.geom.Rectangle2D;

/**
 * Marker used to paint LCS.
 *
 * @author Douglas Lau
 */
public class LcsMarker extends Rectangle2D.Float {

	/** Size (in user coordinates) to render LCS marker */
	static protected final int MARKER_SIZE = 60;

	/** Create a new LCS marker */
	public LcsMarker() {
		this(MARKER_SIZE);
	}

	/** Create a new LCS marker */
	public LcsMarker(float size) {
		super(0, -size / 4, size, size / 2);
	}
}
