/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  AHMCT, University of California
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
package us.mn.state.dot.tms;

import junit.framework.TestCase;

/** 
 * Speed test cases
 *
 * @author Michael Darter
 */
public class SpeedTest extends TestCase {

	/** constructor */
	public SpeedTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		// missing
		assertTrue(new Speed().isMissing());
		assertTrue(new Speed().toKph() == 0d);
		assertFalse(new Speed(0).isMissing());

		// to string
		assertTrue(new Speed().toString().equals(
			PhysicalQuantity.MISSING));
		assertTrue(new Speed().toString2() == null);
		assertTrue(new Speed(5).toString().equals("5 km/h"));

		// conversion
		assertTrue(new Speed(0).toKph() == 0);
		assertTrue(new Speed(0).toMph() == 0);
		assertTrue(new Speed(100).toMph() == 62.13711922373339);
	}
}
