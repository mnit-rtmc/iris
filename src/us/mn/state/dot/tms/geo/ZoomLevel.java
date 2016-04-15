/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.geo;

/**
 * A zoom level describes the amount of zoom on a tile-based mapping system.
 * Zoom level 0 covers the entire planet with 256x256 pixels.  For each
 * successive zoom level, the number of pixels doubles for each dimension.
 *
 * @author Douglas Lau
 */
public enum ZoomLevel {
	ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN,
	ELEVEN, TWELVE, THIRTEEN, FOURTEEN, FIFTEEN, SIXTEEN, SEVENTEEN,
	EIGHTEEN;

	/** Number of tiles in either X or Y dimension. */
	public final int n_tiles = 1 << ordinal();

	/** Number of pixels at the zoom level.  NOTE: for zoom levels
	 * beyond 22, this would need to be declared long. */
	public final int n_pixels = 1 << (ordinal() + 8);

	/** Pixel scale (in meters) at the zoom level */
	public final double scale = circumference() / n_pixels;

	/** Get the circumference of the Earth in meters */
	static private double circumference() {
		return 2 * Math.PI *
			GeodeticDatum.SPHERICAL.getEquatorialRadius();
	}

	/** Get the coordinate origin in meters */
	static private double origin() {
		return Math.PI * GeodeticDatum.SPHERICAL.getEquatorialRadius();
	}

	/** Get a zoom level from an ordinal value */
	static public ZoomLevel fromOrdinal(int o) {
		for(ZoomLevel zl: values()) {
			if(zl.ordinal() == o)
				return zl;
		}
		return null;
	}

	/** Lookup a zoom level for the given scale */
	static public ZoomLevel lookup(double sc) {
		for(ZoomLevel zl: values()) {
			if(zl.scale < sc)
				return zl;
		}
		return values()[values().length - 1];
	}

	/** Get the pixel X coordinate */
	public double getPixelX(double mx) {
		return (mx + origin()) / scale;
	}

	/** Get the pixel Y coordinate */
	public double getPixelY(double my) {
		return (my + origin()) / scale;
	}
}
