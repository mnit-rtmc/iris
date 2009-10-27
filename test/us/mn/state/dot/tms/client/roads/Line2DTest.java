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
 * Line2D test cases
 *
 * @author Douglas Lau
 */
public class Line2DTest extends TestCase {

	public Line2DTest(String name) {
		super(name);
	}

	public void test() {
		Line2D line = new Line2D(0, 0, 10, 0);
		assertTrue(line.project(0, 5).equals(new Vector2D(0, 0)));
		assertTrue(line.project(5, 5).equals(new Vector2D(5, 0)));
		assertTrue(line.project(10, 5).equals(new Vector2D(10, 0)));
		assertTrue(line.project(-5, 0).equals(new Vector2D(-5, 0)));
		assertTrue(line.project(15, 0).equals(new Vector2D(15, 0)));
		assertTrue(line.project(0, -5).equals(new Vector2D(0, 0)));
		assertTrue(line.project(5, -5).equals(new Vector2D(5, 0)));
		assertTrue(line.project(10, -5).equals(new Vector2D(10, 0)));
	}
}
