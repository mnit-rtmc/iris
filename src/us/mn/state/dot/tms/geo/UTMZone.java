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
package us.mn.state.dot.tms.geo;

/**
 * For UTM, there are 60 zones in each hemisphere, numbered from 1-60.  An "N"
 * or "S" is used to determine the hemisphere.  Each zone is 6 degrees wide.
 *
 * @author Douglas Lau
 */
public class UTMZone {

	/** Get the zone number from longitude degrees */
	static private int lon_zone(double lon) {
		return 1 + (int) (Math.floor(lon + 180) / 6);
	}

	/** Zone number */
	private final int number;

	/** Get the zone number */
	public int getNumber() {
		return number;
	}

	/** Northern hemisphere */
	private final boolean hemisphere;

	/** Is the zone in the Northern hemisphere? */
	public boolean isNorthernHemisphere() {
		return hemisphere;
	}

	/** Create a UTM zone */
	public UTMZone(int n, boolean h) {
		if (n < 1 || n > 60) {
			throw new IllegalArgumentException(
				"Invalid zone number:" + n);
		}
		number = n;
		hemisphere = h;
	}

	/** Create a UTM zone for a position */
	public UTMZone(Position pos) {
		this(lon_zone(pos.getLongitude()), pos.getLatitude() >= 0);
	}

	/** Get a string representation of the zone */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(number);
		if (hemisphere)
			b.append('N');
		else
			b.append('S');
		return b.toString();
	}

	/** Test for object equality */
	@Override
	public boolean equals(Object o) {
		if (o instanceof UTMZone) {
			UTMZone oz = (UTMZone) o;
			return number == oz.number &&
			       hemisphere == oz.hemisphere;
		}
		return false;
	}

	/** Get the hash code */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/** Get the zone meridian (degrees longitude) */
	public int meridian() {
		return 6 * number - 183;
	}
}
