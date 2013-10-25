/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.awt.Color;

/**
 * A color as displayed on a DMS.
 *
 * @author Douglas Lau
 */
public class DmsColor {

	/** Get a DMS color from an AWT color */
	static private DmsColor fromColor(Color c) {
		return new DmsColor(c.getRed(), c.getGreen(), c.getBlue());
	}

	/** Clamp one color component to valid range */
	static private int clampComponent(int c) {
		return c > 0 ? (c < 255 ? c : 255) : 0;
	}

	/** Black color */
	static public final DmsColor BLACK = fromColor(Color.BLACK);

	/** Red color */
	static public final DmsColor RED = fromColor(Color.RED);

	/** Yellow color */
	static public final DmsColor YELLOW = fromColor(Color.YELLOW);

	/** Green color */
	static public final DmsColor GREEN = fromColor(Color.GREEN);

	/** Cyan color */
	static public final DmsColor CYAN = fromColor(Color.CYAN);

	/** Blue color */
	static public final DmsColor BLUE = fromColor(Color.BLUE);

	/** Magenta color */
	static public final DmsColor MAGENTA = fromColor(Color.MAGENTA);

	/** White color */
	static public final DmsColor WHITE = fromColor(Color.WHITE);

	/** Orange color */
	static public final DmsColor ORANGE = fromColor(Color.ORANGE);

	/** Amber color (Standard traffic amber) */
	static public final DmsColor AMBER = new DmsColor(255, 208, 0);

	/** Red component of color */
	public final int red;

	/** Green component of color */
	public final int green;

	/** Blue component of color */
	public final int blue;

	/** AWT color */
	public final Color color;

	/** Create a new DMS color */
	public DmsColor(int r, int g, int b) {
		red = clampComponent(r);
		green = clampComponent(g);
		blue = clampComponent(b);
		color = new Color(red, green, blue);
	}

	/** Create a DMS color from an rgb value */
	public DmsColor(int rgb) {
		red = (rgb >> 16) & 0xFF;
		green = (rgb >> 8) & 0xFF;
		blue = rgb & 0xFF;
		color = new Color(red, green, blue);
	}

	/** Test if the color is lit (not black) */
	public boolean isLit() {
		return red > 0 || green > 0 || blue > 0;
	}

	/** Get the 24-bit RGB color value */
	public int rgb() {
		return red << 16 | green << 8 | blue;
	}
}
