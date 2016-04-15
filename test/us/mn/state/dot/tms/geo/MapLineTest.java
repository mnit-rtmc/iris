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
 * MapLine test cases
 *
 * @author Douglas Lau
 */
public class MapLineTest extends TestCase {

	public MapLineTest(String name) {
		super(name);
	}

	public void test() {
		MapLine line = new MapLine(0, 0, 10, 0);
		assertTrue(line.project(0, 5).equals(new MapVector(0, 0)));
		assertTrue(line.project(5, 5).equals(new MapVector(5, 0)));
		assertTrue(line.project(10, 5).equals(new MapVector(10, 0)));
		assertTrue(line.project(-5, 0).equals(new MapVector(-5, 0)));
		assertTrue(line.project(15, 0).equals(new MapVector(15, 0)));
		assertTrue(line.project(0, -5).equals(new MapVector(0, 0)));
		assertTrue(line.project(5, -5).equals(new MapVector(5, 0)));
		assertTrue(line.project(10, -5).equals(new MapVector(10, 0)));
	}
}
