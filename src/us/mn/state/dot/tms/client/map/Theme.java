/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.map;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;

/**
 * A theme is a collection of symbols for one layer of a map.
 *
 * @author Douglas Lau
 */
public class Theme {

	/** Name of theme */
	private final String name;

	/** Symbol to draw */
	private final Symbol symbol;

	/** Default style */
	private final Style def_style;

	/** List of styles */
	private final List<Style> styles = new ArrayList<Style>();

	/** Create a new theme.
	 * @param n Theme name.
	 * @param sym Default symbol.
	 * @param sty Default style. */
	public Theme(String n, Symbol sym, Style sty) {
		name = n;
		symbol = sym;
		def_style = sty;
	}

	/** Get a string representation of the theme */
	@Override
	public String toString() {
		return name;
	}

	/** Add a style to the theme */
	public void addStyle(Style sty) {
		styles.add(sty);
	}

	/** Get a list of all styles */
	public List<Style> getStyles() {
		return styles;
	}

	/** Get the default style */
	public Style getDefaultStyle() {
		return def_style;
	}

	/** Get a style by label */
	public Style getStyle(String label) {
		for (Style sty : styles) {
			if (label.equals(sty.toString()))
				return sty;
		}
		return def_style;
	}

	/** Get style for a map object */
	public Style getStyle(MapObject mo) {
		return def_style;
	}

	/** Set the map scale */
	public void setScale(float scale) {
		symbol.setScale(scale);
	}

	/** Draw the specified map object */
	public void draw(Graphics2D g, MapObject mo) {
		symbol.draw(g, mo, getStyle(mo));
	}

	/** Draw a selected map object */
	public void drawSelected(Graphics2D g, MapObject mo) {
		symbol.drawSelected(g, mo, getStyle(mo));
	}

	/** Hit-test map object */
	public boolean hit(Point2D p, MapObject mo) {
		return symbol.hit(p, mo);
	}

	/** Get tooltip text for the given map object */
	public String getTip(MapObject mo) {
		return mo.toString();
	}

	/** Get a legend icon for a style */
	public Icon getLegend(Style sty) {
		return symbol.getLegend(sty);
	}
	
	/** Get the symbol for this theme */
	public Symbol getSymbol() {
		return symbol;
	}
}
