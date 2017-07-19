/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
 * Copyright (C) 2011       AHMCT, University of California
 * Copyright (C) 2017       Iteris Inc.
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
package us.mn.state.dot.tms.client.weather;

import us.mn.state.dot.tms.client.map.Marker;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Marker used to paint weather sensors.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class WeatherSensorMarker extends Marker {

	/** Size in pixels to render marker */
	static private final int MARKER_SIZE_PIX = UI.scaled(32);

	/** Create a new weather sensor marker */
	public WeatherSensorMarker() {
		super(8);
		final float size = MARKER_SIZE_PIX;
		final float size12 = size / 2f;
		final float size16 = size / 6f;
		path.moveTo(-size12 + size16, -size12); // 1
		path.lineTo(size12 - size16, -size12);  // 2
		path.lineTo(size16, size12);            // 3
		path.lineTo(0, size12 - size16);        // 4
		path.lineTo(-size16, size12);           // 5
		path.closePath();
	}
}
