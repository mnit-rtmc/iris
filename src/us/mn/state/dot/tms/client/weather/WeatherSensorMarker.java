/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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

/**
 * Marker used to paint weather sensors.
 *
 * @author Douglas Lau
 */
public class WeatherSensorMarker extends Marker {

	/** Size in pixels to render marker */
	static protected final int MARKER_SIZE_PIX = 20;

	/** Create a new weather sensor marker */
	public WeatherSensorMarker() {
		super(3);
		float size = MARKER_SIZE_PIX;
		path.moveTo(0, 0);
		path.lineTo(size, size);
		path.closePath();
	}
}
