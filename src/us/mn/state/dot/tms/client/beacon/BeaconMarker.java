/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.beacon;

import us.mn.state.dot.tms.client.map.Marker;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Marker used to paint beacons.
 *
 * @author Douglas Lau
 */
public class BeaconMarker extends Marker {

	/** Size in pixels to render marker */
	static private final int MARKER_SIZE_PIX = UI.scaled(16);

	/** Create a new beacon marker */
	public BeaconMarker() {
		super(22);
		float size = MARKER_SIZE_PIX;
		float half = size * 0.5f;
		float s2 = size * (float) Math.sqrt(0.5f);
		float s1 = size * (1 - (float) Math.sqrt(0.5f));
		path.moveTo(0, -size);
		path.lineTo(s1, -s2);
		path.lineTo(s2, -s2);
		path.lineTo(s2, -s1);
		path.lineTo(size, 0);
		path.lineTo(s2, s1);
		path.lineTo(s2, s2);
		path.lineTo(s1, s2);
		path.lineTo(0, size);
		path.lineTo(-s1, s2);
		path.lineTo(-s2, s2);
		path.lineTo(-s2, s1);
		path.lineTo(-size, 0);
		path.lineTo(-s2, -s1);
		path.lineTo(-s2, -s2);
		path.lineTo(-s1, -s2);
		path.lineTo(0, -size);
		path.closePath();
		path.moveTo(0, s2);
		path.lineTo(s1, -half);
		path.lineTo(-s1, -half);
		path.closePath();
	}
}
