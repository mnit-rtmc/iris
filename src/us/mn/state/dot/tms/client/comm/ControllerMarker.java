/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.map.AbstractMarker;

/**
 * Marker used to paint controllers.
 *
 * @author Douglas Lau
 */
public class ControllerMarker extends AbstractMarker {

	/** Size in pixels to render marker */
	static protected final int MARKER_SIZE_PIX = 24;
	static protected final int WIDTH = MARKER_SIZE_PIX;
	static protected final int HEIGHT = 3 * MARKER_SIZE_PIX / 4;
	static protected final float W2 = WIDTH / 2.0f;
	static protected final float H2 = HEIGHT / 2.0f;
	static protected final float W2_5 = WIDTH * 2.0f / 5;
	static protected final float W3_5 = WIDTH * 3.0f / 5;
	static protected final float H1_5 = HEIGHT / 5.0f;
	static protected final float H2_5 = HEIGHT * 2.0f / 5;
	static protected final float H3_5 = HEIGHT * 3.0f / 5;
	static protected final float H4_5 = HEIGHT * 4.0f / 5;
	static protected final float H10 = HEIGHT / 10.0f;
	static protected final float H12 = HEIGHT / 12.0f;
	static protected final float H20 = HEIGHT / 20.0f;
	static protected final float H24 = HEIGHT / 24.0f;

	/** Create a new controller marker */
	public ControllerMarker() {
		super(20);
		// Controller outline
		path.moveTo(0, H1_5);
		path.lineTo(WIDTH - H20, H1_5);
		path.lineTo(WIDTH - H20, H4_5);
		path.lineTo(0, H4_5);
		path.lineTo(0, H1_5);
		// LED Screen
		path.moveTo(W2_5 - H24, H4_5 - H20);
		path.lineTo(W3_5 + H24, H4_5 - H20);
		path.lineTo(W3_5 + H24, H3_5);
		path.lineTo(W2_5 - H24, H3_5);
		path.lineTo(W2_5 - H24, H4_5 - H20);
		// Buttons
		addButton(W2_5, H2_5 - H10);
		addButton(W2, H2_5 - H10);
		addButton(W3_5, H2_5 - H10);
		addButton(W2_5, H2_5);
		addButton(W2, H2_5);
		addButton(W3_5, H2_5);
		addButton(W2_5, H2_5 + H10);
		addButton(W2, H2_5 + H10);
		addButton(W3_5, H2_5 + H10);
		path.closePath();
	}

	/** Add a button to the marker */
	protected void addButton(float w, float h) {
		Arc2D.Float arc = new Arc2D.Float(w - H24, h - H24,
			H12, H12, 0, 360, Arc2D.OPEN);
		path.append(arc, false);
	}
}
