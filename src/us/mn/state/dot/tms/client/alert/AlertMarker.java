/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
package us.mn.state.dot.tms.client.alert;

import us.mn.state.dot.tms.client.map.Marker;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Marker used to paint alerts.
 *
 * @author Douglas Lau
 */
public class AlertMarker extends Marker {

	/** Size in pixels to render marker */
	static private final int MARKER_SIZE_PIX = UI.scaled(36);

	/** Create a new incident marker */
	public AlertMarker() {
		super(6);
		
		float size = MARKER_SIZE_PIX;
		float half = size / 2;
		float quarter = size / 4;
		path.moveTo(0, 0);
		path.lineTo(half, size);
		path.lineTo(size, 0);
		path.lineTo(size-quarter, 0);
		path.lineTo(half, half);
		path.lineTo(quarter, 0);
		path.closePath();
	}
}
