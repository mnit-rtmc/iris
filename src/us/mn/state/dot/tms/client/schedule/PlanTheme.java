/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import java.awt.Color;
import java.awt.Shape;
import us.mn.state.dot.map.Style;
import us.mn.state.dot.map.VectorSymbol;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * Theme for action plans.
 *
 * @author Douglas Lau
 */
public class PlanTheme extends ProxyTheme<ActionPlan> {

	/** Legend color */
	static private final Color COLOR = new Color(255, 255, 0, 128);

	/** Action plan object marker */
	static private final PlanMarker MARKER = new PlanMarker();

	/** Create a new plan theme */
	public PlanTheme(PlanManager man) {
		super(man, MARKER);
	}

	/** Add a default style to the theme */
	@Override
	public void addStyle(ItemStyle is) {
		Style style = new Style(is.toString(), OUTLINE, COLOR);
		addStyle(style);
	}

	/** Add a new style to the theme */
	public void addStyle(ItemStyle is, Shape shp) {
		Style style = new Style(is.toString(), OUTLINE, COLOR);
		styles.add(style);
		addSymbol(new VectorSymbol(style, shp));
	}

	/** Add a new style to the theme */
	public void addStyle(ItemStyle is, Shape shp, Color clr) {
		Style style = new Style(is.toString(), OUTLINE, clr);
		styles.add(style);
		addSymbol(new VectorSymbol(style, shp));
	}
}
