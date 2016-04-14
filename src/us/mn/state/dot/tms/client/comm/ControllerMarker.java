/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.awt.geom.Arc2D;
import us.mn.state.dot.tms.client.map.Marker;

/**
 * Marker used to paint controllers.
 *
 * @author Douglas Lau
 */
public class ControllerMarker extends Marker {

	/** Size in pixels to render marker */
	static protected final int MARKER_SIZE_PIX = 24;
	static protected final int WIDTH = MARKER_SIZE_PIX;
	static protected final int HEIGHT = 3 * MARKER_SIZE_PIX / 5;
	static protected final float W2 = WIDTH / 2.0f;
	static protected final float H2 = HEIGHT / 2.0f;
	static protected final float W1_5 = WIDTH / 5.0f;
	static protected final float H1_5 = HEIGHT / 5.0f;
	static protected final float H2_5 = HEIGHT * 2.0f / 5;
	static protected final float H12 = HEIGHT / 12.0f;
	static protected final float H24 = HEIGHT / 24.0f;

	/** Create a new controller marker */
	public ControllerMarker() {
		super(20);
		// Controller outline
		path.moveTo(-W2, -H2);
		path.lineTo(W2, -H2);
		path.lineTo(W2, H2);
		path.lineTo(-W2, H2);
		path.lineTo(-W2, -H2);
		// LED Screen
		path.moveTo(-W1_5, H1_5);
		path.lineTo(W1_5, H1_5);
		path.lineTo(W1_5, H2_5);
		path.lineTo(-W1_5, H2_5);
		path.lineTo(-W1_5, H1_5);
		// Buttons
		addButton(-W1_5, 0);
		addButton(0, 0);
		addButton(W1_5, 0);
		addButton(-W1_5, -H1_5);
		addButton(0, -H1_5);
		addButton(W1_5, -H1_5);
		addButton(-W1_5, -H2_5);
		addButton(0, -H2_5);
		addButton(W1_5, -H2_5);
		path.closePath();
	}

	/** Add a button to the marker */
	protected void addButton(float w, float h) {
		Arc2D.Float arc = new Arc2D.Float(w - H24, h,
			H12, H12, 0, 360, Arc2D.OPEN);
		path.append(arc, false);
	}
}
