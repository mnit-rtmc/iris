/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
 * GeodeticDatum test cases.
 *
 * @author Doug Lau
 */
public class GeodeticDatumTest extends TestCase {

	static private final double EPSILON = 0.000000002;	// 2 nm

	static boolean near(double v0, double v1) {
		return (v0 - EPSILON <= v1)
		    && (v0 + EPSILON >= v1);
	}

	public GeodeticDatumTest(String name) {
		super(name);
	}

	public void testMeanRadius() {
		assertTrue(near(GeodeticDatum.WGS_84.getMeanRadius(),
		                6371008.771415));
	}
}
