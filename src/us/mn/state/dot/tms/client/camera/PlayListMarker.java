/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.client.map.Marker;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Marker used to paint play lists.
 *
 * @author Douglas Lau
 */
public class PlayListMarker extends Marker {

	/** Size in pixels to render marker */
	static private final int MARKER_SIZE_PIX = UI.scaled(24);

	/** Create a new play list marker */
	public PlayListMarker() {
		super(12);
		float size = MARKER_SIZE_PIX;
		float s2 = size / 2;
		float s3 = size / 3;
		float s5 = size / 5;
		path.moveTo(-s2, -s2);
		path.lineTo(s2, -s2);
		path.lineTo(s2, s2);
		path.lineTo(-s2, s2);
		path.lineTo(-s2, -s2);
		path.moveTo(-s3, -s5);
		path.lineTo(s3, -s5);
		path.moveTo(-s3, 0);
		path.lineTo(s3, 0);
		path.moveTo(-s3, s5);
		path.lineTo(s3, s5);
		path.closePath();
	}
}
