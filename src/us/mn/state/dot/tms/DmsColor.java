/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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

/**
 * A color as displayed on a DMS.
 *
 * @author Douglas Lau
 */
public class DmsColor {

	/** Clamp one color component to valid range */
	static private int clampComponent(int c) {
		return c > 0 ? (c < 255 ? c : 255) : 0;
	}

	/** Black color */
	static public final DmsColor BLACK = new DmsColor(0, 0, 0);

	/** Amber color (Standard traffic amber) */
	static public final DmsColor AMBER = new DmsColor(255, 208, 0);

	/** Red component of color */
	public final int red;

	/** Green component of color */
	public final int green;

	/** Blue component of color */
	public final int blue;

	/** Create a new DMS color */
	public DmsColor(int r, int g, int b) {
		red = clampComponent(r);
		green = clampComponent(g);
		blue = clampComponent(b);
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
