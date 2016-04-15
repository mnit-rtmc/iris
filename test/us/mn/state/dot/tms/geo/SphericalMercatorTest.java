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

import junit.framework.TestCase;

/** 
 * Spherical mercator convertion test cases.
 *
 * @author Doug Lau
 */
public class SphericalMercatorTest extends TestCase {

	static protected final double EPSILON = 0.000000002;

	static boolean near(double v0, double v1) {
		return v0 - EPSILON <= v1 && v0 + EPSILON >= v1;
	}

	public SphericalMercatorTest(String name) {
		super(name);
	}

	public void test() {
		SphericalMercatorPosition smp;
		smp = testPosition(45, -93);	// Minnesota
		assertTrue(near(smp.getX(), -10352712.643774442));
		assertTrue(near(smp.getY(), 5621521.486192066));
		smp = testPosition(45, -94);	// Minnesota
		assertTrue(near(smp.getX(), -10464032.134567715));
		assertTrue(near(smp.getY(), 5621521.486192066));
		smp = testPosition(39, -122);	// California
		assertTrue(near(smp.getX(), -13580977.876779376));
		assertTrue(near(smp.getY(), 4721671.572580107));
		smp = testPosition(-45, 173);	// New Zealand
		assertTrue(near(smp.getX(), 19258271.907236326));
		assertTrue(near(smp.getY(), -5621521.486192067));
	}

	protected SphericalMercatorPosition testPosition(double lat,double lon){
		SphericalMercatorPosition smp =
			SphericalMercatorPosition.convert(new Position(lat,
			lon));
		System.err.println(smp);
		Position p = smp.getPosition();
		assertTrue(near(p.getLatitude(), lat));
		assertTrue(near(p.getLongitude(), lon));
		return smp;
	}
}
