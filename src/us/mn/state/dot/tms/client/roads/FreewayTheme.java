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
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A theme for drawing segment objects in a dull gray color.
 *
 * @author Douglas Lau
 */
public class FreewayTheme extends SegmentTheme {

	/** Default freeway style theme */
	static private final Style CLEAR_STYLE = new Style("",
		OUTLINE, new Color(0, 0, 0, 0.1f));

	/** Create a new freeway theme */
	public FreewayTheme() {
		super(I18N.get("location.freeway"));
		addStyle(CLEAR_STYLE);
	}

	/** Get the style to draw a given segment */
	@Override
	protected Style getSegmentStyle(MapSegment ms) {
		return CLEAR_STYLE;
	}
}
