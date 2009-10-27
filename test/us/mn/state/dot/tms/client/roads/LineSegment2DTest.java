/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import junit.framework.TestCase;

/** 
 * LineSegment2D test cases
 *
 * @author Douglas Lau
 */
public class LineSegment2DTest extends TestCase {

	public LineSegment2DTest(String name) {
		super(name);
	}

	public void test() {
		LineSegment2D seg = new LineSegment2D(0, 0, 10, 0);
		assertTrue(seg.distanceTo(0, 5) == 5);
		assertTrue(seg.distanceTo(5, 5) == 5);
		assertTrue(seg.distanceTo(10, 5) == 5);
		assertTrue(seg.distanceTo(-5, 0) == 5);
		assertTrue(seg.distanceTo(15, 0) == 5);
		assertTrue(seg.distanceTo(0, -5) == 5);
		assertTrue(seg.distanceTo(5, -5) == 5);
		assertTrue(seg.distanceTo(10, -5) == 5);
	}
}
