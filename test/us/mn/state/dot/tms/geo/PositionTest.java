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

import junit.framework.TestCase;

/** 
 * Position test cases.
 *
 * @author Doug Lau
 */
public class PositionTest extends TestCase {

	static protected final double EPSILON = 0.000000002;

	static boolean near(double v0, double v1) {
		return v0 - EPSILON <= v1 && v0 + EPSILON >= v1;
	}

	static final double[][] COORDS = {
		{ 45, -93.1, 7862.679274615 },
		{ 44.9, -93.1, 13622.519162252 },
		{ 44.9, -93.0, 11119.508372419 },
		{ 45.1, -93.0, 11119.508372419 }
	};

	public PositionTest(String name) {
		super(name);
	}

	public void test() {
		Position p = new Position(45, -93);
		for(double[] coord: COORDS) {
			Position p2 = new Position(coord[0], coord[1]);
			testDistance(p, p2, coord[2]);
		}
	}

	private void testDistance(Position p, Position p2, double dist) {
		double dh = p.distanceHaversine(p2);
		System.err.println("distance: " + dist + ", " + dh);
		assertTrue(near(dist, dh));
	}
}
