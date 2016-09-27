/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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
 * A position in the spherical mercator projection.
 *
 * @author Douglas Lau
 */
public class SphericalMercatorPosition {

	/** Largest absolute value of latitude */
	static private final double MAX_LATITUDE = 85.05112878;

	/** Clip latitude to valid range */
	static private double clipLatitude(double lat) {
		return Math.max(Math.min(lat, MAX_LATITUDE), -MAX_LATITUDE);
	}

	/** Convert a (lat/lon) position to spherical mercator */
	static public SphericalMercatorPosition convert(Position pos) {
		double lat = clipLatitude(pos.getLatitude());
		double radius = GeodeticDatum.SPHERICAL.getEquatorialRadius();
		double mx = Math.toRadians(pos.getLongitude()) * radius;
		double rlat = Math.toRadians(lat + 90) / 2;
		double my = Math.log(Math.tan(rlat)) * radius;
		return new SphericalMercatorPosition(mx, my);
	}

	/** X (meters) from origin */
	private final double x;

	/** Get the X coordinate (meters) */
	public double getX() {
		return x;
	}

	/** Y (meters) from origin */
	private final double y;

	/** Get the Y coordinate (meters) */
	public double getY() {
		return y;
	}

	/** Create a new spherical mercator position */
	public SphericalMercatorPosition(double mx, double my) {
		x = mx;
		y = my;
	}

	/** Get a string representation of the position */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(x);
		b.append(',');
		b.append(y);
		return b.toString();
	}

	/** Get the (lat/lon) position */
	public Position getPosition() {
		double radius = GeodeticDatum.SPHERICAL.getEquatorialRadius();
		double rlat = Math.atan(Math.exp(y / radius));
		double lat = Math.toDegrees(2 * rlat) - 90;
		double lon = Math.toDegrees(x / radius);
		return new Position(lat, lon);
	}
}
