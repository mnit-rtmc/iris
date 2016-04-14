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
package us.mn.state.dot.tms.client.lcs;

import us.mn.state.dot.tms.client.map.AbstractMarker;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Marker used to paint LCS.
 *
 * @author Douglas Lau
 */
public class LcsMarker extends AbstractMarker {

	/** Size in pixels to render marker */
	static private final int MARKER_SIZE_PIX = UI.scaled(24);

	/** Create a new LCS marker */
	public LcsMarker() {
		super(14);
		float size = MARKER_SIZE_PIX;
		float tiny = size / 16;
		float third = size / 3;
		float half = size / 2;
		float x = 0;
		float y = half / 2;
		path.moveTo(x, y);
		path.lineTo(x += third, y);
		path.lineTo(x, y -= tiny);
		path.lineTo(x += third, y);
		path.lineTo(x, y += tiny);
		path.lineTo(x += third, y);
		path.lineTo(x, y -= half);
		path.lineTo(x -= third, y);
		path.lineTo(x, y += tiny);
		path.lineTo(x -= third, y);
		path.lineTo(x, y -= tiny);
		path.lineTo(x -= third, y);
		path.lineTo(x, y += half);
		path.closePath();
	}
}
