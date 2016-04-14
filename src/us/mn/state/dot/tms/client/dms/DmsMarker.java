/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Marker used to paint DMS.
 *
 * @author Douglas Lau
 */
public class DmsMarker extends Marker {

	/** Size in pixels to render marker */
	static private final int MARKER_SIZE_PIX = UI.scaled(32);

	/** Create a new DMS marker */
	public DmsMarker() {
		super(13);
		float size = MARKER_SIZE_PIX;
		float height = 3 * size / 5;
		float half_width = size / 2;
		float third_width = size / 3;
		float fifth_width = size / 5;
		float half_height = height / 2;
		float x = size;
		float y = 0;
		path.moveTo(x -= half_width, y -= half_height);
		path.lineTo(x += size, y);
		path.lineTo(x, y += fifth_width);
		path.lineTo(x -= fifth_width, y);
		path.lineTo(x, y += third_width);
		path.lineTo(x -= fifth_width, y);
		path.lineTo(x, y -= third_width);
		path.lineTo(x -= fifth_width, y);
		path.lineTo(x, y += third_width);
		path.lineTo(x -= fifth_width, y);
		path.lineTo(x, y -= third_width);
		path.lineTo(x -= fifth_width, y);
		path.closePath();
	}
}
