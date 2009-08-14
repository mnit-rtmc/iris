/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.Outline;
import us.mn.state.dot.map.Style;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;

/**
 * A simple theme which uses one symbol to draw all segment objects.
 *
 * @author Douglas Lau
 */
abstract public class SegmentTheme extends StyledTheme {

	/** Color for rendering gray stations */
	static public final Color GRAY = Color.GRAY;

	/** Color for rendering green stations */
	static public final Color GREEN = new Color(48, 160, 48);

	/** Color for rendering yellow stations */
	static public final Color YELLOW = new Color(240, 240, 0);

	/** Color for rendering orange stations */
	static public final Color ORANGE = new Color(255, 192, 0);

	/** Color for rendering red stations */
	static public final Color RED = new Color(208, 0, 0);

	/** Color for rendering violet stations */
	static public final Color VIOLET = new Color(192, 0, 240);

	/** Default segment style theme */
	static protected final Style DEFAULT_STYLE = new Style("No Data", GRAY);

	/** Create a new segment theme */
	protected SegmentTheme(String name) {
		super(name, new Rectangle(0, 0, 200, 200));
		addStyle(DEFAULT_STYLE);
	}

	/** Draw the specified map object */
	public void draw(Graphics2D g, MapObject mo) {
		getSymbol(mo).draw(g, mo.getShape());
	}

	/** Draw a selected map object */
	public void drawSelected(Graphics2D g, MapObject mo) {
		Shape shape = mo.getShape();
		Outline outline = Outline.createDashed(Color.WHITE, 20);
		g.setColor(outline.color);
		g.setStroke(outline.stroke);
		g.draw(shape);
		outline = Outline.createSolid(Color.WHITE, getThickness(shape));
		Shape ellipse = createEllipse(shape);
		g.setStroke(outline.stroke);
		g.draw(ellipse);
	}

	/** Get the style to draw a given map object */
	public Style getStyle(MapObject mo) {
		MapSegment ms = (MapSegment)mo;
		return getStyle(ms);
	}

	/** Get the style to draw a given segment */
	abstract protected Style getStyle(MapSegment ms);

	/** Get the tooltip text for a given segment */
	public String getTip(MapObject mo) {
		MapSegment ms = (MapSegment)mo;
		StringBuffer b = new StringBuffer("Station ");
		String sid = ms.getStationID();
		b.append(sid);
		b.append(": ");
		Station sta = StationHelper.lookup(sid);
		if(sta != null)
			b.append(sta.getLabel());
		b.append("\n Flow = ");
		b.append(ms.getFlow());
		b.append("\n Density = ");
		b.append(ms.getDensity());
		b.append("\n Speed = ");
		b.append(ms.getSpeed());
		return b.toString();
	}
}
