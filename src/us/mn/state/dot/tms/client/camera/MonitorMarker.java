/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
 * Marker used to paint video monitors.
 *
 * @author Douglas Lau
 */
public class MonitorMarker extends Marker {

	/** Size in pixels to render marker */
	static private final int MARKER_SIZE_PIX = UI.scaled(24);

	/** Create a new monitor marker */
	public MonitorMarker() {
		super(15);
		float size = MARKER_SIZE_PIX;
		float s2 = size / 2;
		float s3 = size / 3;
		float s4 = size / 4;
		float s5 = size / 5;
		path.moveTo(-s2, -s3);
		path.lineTo(s2, -s3);
		path.lineTo(s2, s3);
		path.lineTo(-s2, s3);
		path.lineTo(-s2, -s3);
		path.moveTo(-s4, -s4);
		path.lineTo(s4, -s4);
		path.quadTo(s3, -s4, s3, -s5);
		path.lineTo(s3, s5);
		path.quadTo(s3, s4, s4, s4);
		path.lineTo(-s4, s4);
		path.quadTo(-s3, s4, -s3, s5);
		path.lineTo(-s3, -s5);
		path.quadTo(-s3, -s4, -s4, -s4);
		path.closePath();
	}
}
