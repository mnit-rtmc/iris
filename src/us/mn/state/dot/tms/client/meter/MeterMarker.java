/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.meter;

import java.awt.geom.Arc2D;
import us.mn.state.dot.map.marker.AbstractMarker;

/**
 * Marker used to paint ramp meters.
 *
 * @author Douglas Lau
 */
public class MeterMarker extends AbstractMarker {

	/** Size (in user coordinates) to render ramp meter marker */
	static protected final int MARKER_SIZE = 500;

	/** Create a new ramp meter marker */
	public MeterMarker() {
		this(MARKER_SIZE);
	}

	/** Create a new ramp meter marker */
	public MeterMarker(float size) {
		super(4);
		path.moveTo(0, 0);
		Arc2D.Float arc = new Arc2D.Float(0, -size, size, size,
			-90, 270, Arc2D.OPEN);
		path.append(arc, true);
		path.closePath();
		path.lineTo(size / 2, -size / 2);
	}
}
