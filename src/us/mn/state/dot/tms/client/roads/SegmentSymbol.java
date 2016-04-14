/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.awt.Graphics2D;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.Style;
import us.mn.state.dot.map.VectorSymbol;

/**
 * Symbol to draw map segments.
 *
 * @author Douglas Lau
 */
public class SegmentSymbol extends VectorSymbol {

	/** R_Node marker */
	private final R_NodeMarker marker = new R_NodeMarker();

	/** Create a new segment symbol */
	public SegmentSymbol(int lsize) {
		super(new RectMarker(), lsize);
	}

	/** Set the map scale */
	@Override
	public void setScale(float s) {
		setScale(s, marker);
	}

	/** Draw a selected symbol */
	@Override
	public void drawSelected(Graphics2D g, MapObject mo, Style sty) {
		super.draw(g, mo, sty);
		super.drawSelected(g, mo, sty);
	}
}
