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

import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A theme for drawing segment objects based on density thresholds.
 *
 * @author Douglas Lau
 */
public class DensityTheme extends SegmentTheme {

	/** Density styles */
	static private final Style[] D_STYLES = new Style[] {
		new Style(I18N.get("units.density.low"), OUTLINE, GREEN),
		new Style(I18N.get("units.density.medium"), OUTLINE, ORANGE),
		new Style(I18N.get("units.density.high"), OUTLINE, RED),
		new Style(I18N.get("units.data.bad"), OUTLINE, VIOLET)
	};

	/** Create a new density theme */
	public DensityTheme() {
		super(I18N.get("units.density"));
		for (Style s: D_STYLES)
			addStyle(s);
	}

	/** Get the style to draw a given segment */
	@Override
	protected Style getSegmentStyle(MapSegment ms) {
		Integer d = ms.getDensity();
		if (d == null)
			return DEFAULT_STYLE;
		if (d < 30)
			return D_STYLES[0];
		if (d < 50)
			return D_STYLES[1];
		if (d < 200)
			return D_STYLES[2];
		return D_STYLES[3];
	}
}
