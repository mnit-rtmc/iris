/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.units;

import junit.framework.TestCase;

/**
 * Distance tests
 *
 * @author Douglas Lau
 */
public class DistanceTest extends TestCase {

	public DistanceTest(String name) {
		super(name);
	}

	public void test() {
		Distance.Formatter df = new Distance.Formatter(2);
		assertTrue(df.format(new Distance(1)).equals("1.00 m"));
		assertTrue(df.format(new Distance(5)).equals("5.00 m"));
		assertTrue(df.format(new Distance(1).convert(
			Distance.Units.FEET)).equals("3.28 ft"));
		assertTrue(df.format(new Distance(1).convert(
			Distance.Units.INCHES)).equals("39.37 in"));
		assertTrue(df.format(new Distance(1, Distance.Units.MILES).
			convert(Distance.Units.KILOMETERS)).equals("1.61 km"));
		assertTrue(df.format(new Distance(1, Distance.Units.INCHES).
			convert(Distance.Units.CENTIMETERS)).equals("2.54 cm"));
		assertTrue(df.format(new Distance(1, Distance.Units.YARDS).
			convert(Distance.Units.DECIMETERS)).equals("9.14 dm"));
		assertTrue(df.format(new Distance(1, Distance.Units.FEET).
			convert(Distance.Units.MILLIMETERS)).equals("304.80 mm"));
	}
}
