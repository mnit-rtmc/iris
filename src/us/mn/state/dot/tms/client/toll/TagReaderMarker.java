/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toll;

import java.awt.geom.Arc2D;
import us.mn.state.dot.tms.client.map.AbstractMarker;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Marker used to paint tag readers.
 *
 * @author Douglas Lau
 */
public class TagReaderMarker extends AbstractMarker {

	/** Size in pixels to render marker */
	static private final int MARKER_SIZE_PIX = UI.scaled(28);

	/** Scale an ordinate value */
	static private float fscale(int i) {
		return (i * MARKER_SIZE_PIX) / 128f;
	}

	/** Make an arc */
	static private Arc2D.Float makeArc(int s, int st, int ex) {
		return new Arc2D.Float(fscale(-s), fscale(-s),
			fscale(s * 2), fscale(s * 2), st, ex, Arc2D.OPEN);
	}

	/** Create a new tag reader marker */
	public TagReaderMarker() {
		super(4);
		path.moveTo(fscale(-37), 0);
		path.lineTo(0, fscale(37));
		path.lineTo(fscale(37), 0);
		path.append(makeArc(37, 0, 180), true);
		path.closePath();
		path.append(makeArc(52, -210, -120), false);
		path.append(makeArc(66, 35, 110), true);
		path.closePath();
		path.append(makeArc(80, -218, -104), false);
		path.append(makeArc(94, 40, 100), true);
		path.closePath();
		path.append(makeArc(108, -222, -96), false);
		path.append(makeArc(122, 43, 94), true);
		path.closePath();
	}
}
