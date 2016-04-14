/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.client.map.Outline;
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.client.map.Theme;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A simple theme which uses one symbol to draw all segment objects.
 *
 * @author Douglas Lau
 */
abstract public class SegmentTheme extends Theme {

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

	/** Transparent black outline */
	static protected final Outline OUTLINE = Outline.createSolid(
		new Color(0, 0, 0, 128), 0.6f);

	/** Default segment style theme */
	static protected final Style DEFAULT_STYLE = new Style(I18N.get(
		"detector.no.data"), OUTLINE, GRAY);

	/** R_node style theme */
	static private final Style R_NODE_STYLE = new Style(I18N.get("r_node"),
		OUTLINE, new Color(255, 96, 128, 128));

	/** Size of legend icons */
	static private final int lsize = UI.scaled(22);

	/** Create a new segment theme */
	protected SegmentTheme(String name) {
		super(name, new SegmentSymbol(lsize));
		addStyle(DEFAULT_STYLE);
		addStyle(R_NODE_STYLE);
	}

	/** Get the style to draw a given map object */
	@Override
	public Style getStyle(MapObject mo) {
		if (mo instanceof MapSegment) {
			MapSegment ms = (MapSegment) mo;
			return getSegmentStyle(ms);
		} else
			return R_NODE_STYLE;
	}

	/** Get the style to draw a given segment */
	abstract protected Style getSegmentStyle(MapSegment ms);

	/** Get the tooltip text for a given segment */
	@Override
	public String getTip(MapObject mo) {
		if (mo instanceof MapSegment)
			return ((MapSegment) mo).getTip();
		else
			return null;
	}
}
