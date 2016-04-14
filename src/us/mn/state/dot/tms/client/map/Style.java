/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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

import java.awt.Color;

/**
 * A style for stroke/fill of map objects.
 *
 * @author Douglas Lau
 */
public class Style {

	/** Style name */
	private final String name;

	/** Outline style */
	public final Outline outline;

	/** Fill color */
	public final Color fill_color;

	/** Display on legend */
	public final boolean legend;

	/** Create a style.
	 * @param n Style name.
	 * @param o Outline.
	 * @param f Fill color.
	 * @param l Legend flag. */
	public Style(String n, Outline o, Color f, boolean l) {
		name = n;
		outline = o;
		fill_color = f;
		legend = l;
	}

	/** Create a style.
	 * @param n Style name.
	 * @param o Outline.
	 * @param f Fill color. */
	public Style(String n, Outline o, Color f) {
		this(n, o, f, true);
	}

	/** Create a style.
	 * @param n Style name.
	 * @param f Fill color. */
	public Style(String n, Color f) {
		this(n, null, f, true);
	}

	/** Create a style.
	 * @param n Style name. */
	public Style(String n) {
		this(n, null, null, false);
	}

	/** Get the style name */
	@Override
	public String toString() {
		return name;
	}
}
