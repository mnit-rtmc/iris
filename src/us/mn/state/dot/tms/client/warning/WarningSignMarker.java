/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.warning;

import java.awt.geom.Ellipse2D;
import us.mn.state.dot.map.AbstractMarker;

/**
 * Marker used to paint warning signs.
 *
 * @author Douglas Lau
 */
public class WarningSignMarker extends AbstractMarker {

	/** Size in pixels to render marker */
	static protected final int MARKER_SIZE_PIX = 20;

	/** Create a new warning sign marker */
	public WarningSignMarker() {
		super(10);
		float size = MARKER_SIZE_PIX;
		float sixth = size / 6;
		float third = size / 3;
		float half = size / 2;
		path.moveTo(half, half);
		path.lineTo(half + third, sixth);
		path.lineTo(half + sixth, sixth);
		path.lineTo(half + sixth, -half);
		path.lineTo(half - sixth, -half);
		path.lineTo(half - sixth, sixth);
		path.lineTo(half - third, sixth);
		path.closePath();
		path.append(new Ellipse2D.Float(0, -sixth, third, third),
			false);
		path.append(new Ellipse2D.Float(half + sixth, -sixth,
			third, third), false);
	}
}
