/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2016  Minnesota Department of Transportation
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
import java.awt.Shape;
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

	/** Default symbol */
	private final Symbol def_symbol;

	/** Default style */
	private Style def_style;

	/** List of styles */
	private final List<Style> styles = new ArrayList<Style>();

	/** Create a new theme.
	 * @param n Theme name.
	 * @param sym Default symbol. */
	public Theme(String n, Symbol sym) {
		name = n;
		def_symbol = sym;
	}

	/** Get a string representation of the theme */
	@Override
	public String toString() {
		return name;
	}

	/** Add a style to the theme */
	public void addStyle(Style sty) {
		styles.add(sty);
		if (def_style == null)
			def_style = sty;
	}

	/** Get a style by label */
	public Style getStyle(String label) {
		for (Style sty : styles) {
			if (label.equals(sty.toString()))
				return sty;
		}
		return def_style;
	}

	/** Get a list of all legend styles */
	public List<Style> getStyles() {
		ArrayList<Style> s = new ArrayList<Style>();
		for (Style sty : styles) {
			if (sty.legend)
				s.add(sty);
		}
		return s;
	}

	/** Get style for a map object */
	public Style getStyle(MapObject mo) {
		return def_style;
	}

	/** Set the map scale */
	public void setScale(float scale) {
		def_symbol.setScale(scale);
	}

	/** Draw the specified map object */
	public void draw(Graphics2D g, MapObject mo) {
		def_symbol.draw(g, mo, getStyle(mo));
	}

	/** Draw a selected map object */
	public void drawSelected(Graphics2D g, MapObject mo) {
		def_symbol.drawSelected(g, mo, getStyle(mo));
	}

	/** Hit-test map object */
	public boolean hit(Point2D p, MapObject mo) {
		return def_symbol.hit(p, mo);
	}

	/** Get tooltip text for the given map object */
	public String getTip(MapObject mo) {
		return mo.toString();
	}

	/** Get a legend icon for a style */
	public Icon getLegend(Style sty) {
		return def_symbol.getLegend(sty);
	}
}
