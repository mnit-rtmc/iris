/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gate;

import us.mn.state.dot.tms.client.map.AbstractMarker;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Marker used to paint gate arms.
 *
 * @author Douglas Lau
 */
public class GateArmMarker extends AbstractMarker {

	/** Size in pixels to render marker */
	static private final int MARKER_SIZE_PIX = UI.scaled(24);

	/** Create a new gate arm marker */
	public GateArmMarker() {
		super(10);
		float size = MARKER_SIZE_PIX;
		float fifth = size / 5;
		float quarter = size / 4;
		float third = size / 3;
		float half = size / 2;
		path.moveTo(fifth, half);
		path.lineTo(size, size - quarter);
		path.lineTo(size, size - third);
		path.lineTo(quarter, third);
		path.lineTo(quarter, half);
		path.moveTo(0, half);
		path.lineTo(quarter, half);
		path.lineTo(quarter, 0);
		path.lineTo(0, 0);
		path.closePath();
	}
}
