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
 * Temperature test cases
 *
 * @author Michael Darter
 */
public class TemperatureTest extends TestCase {

	/** constructor */
	public TemperatureTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		// no args constructor
		assertTrue(new Temperature().isMissing());
		assertTrue(new Temperature().toC() == 0d);

		// string constructor
		String nullstring = null;
		assertTrue(new Temperature(nullstring).isMissing());
		assertTrue(new Temperature("").isMissing());
		assertFalse(new Temperature("0").isMissing());

		// int constructor
		Integer nullint = null;
		assertTrue(new Temperature(nullint).isMissing());
		assertFalse(new Temperature(0).isMissing());
		assertTrue(new Temperature(new Integer(3)).toC() == 3);

		// double constructor
		Double nulldouble = null;
		assertTrue(new Temperature(nulldouble).isMissing());
		assertFalse(new Temperature(0d).isMissing());
		assertTrue(new Temperature(3.8d).toC() == 3.8);

		// round
		assertTrue(new Temperature(3.123).round(0).toC() == 3);
		assertTrue(new Temperature(3.123).round(1).toC() == 3.1);
		assertTrue(new Temperature(3.123).round(2).toC() == 3.12);

		// to string
		assertTrue(new Temperature().toString().equals(
			PhysicalQuantity.MISSING));
		assertTrue(new Temperature(5).toString().equals(
			"5 \u00B0C"));

		// to string2
		assertTrue(new Temperature().toString2() == null);
		assertTrue(new Temperature(5).toString2().equals(
			"5 \u00B0C"));

		// to Integer
		assertTrue(new Temperature().toCInteger() == null);
		assertFalse(new Temperature(new Integer(3)).isMissing());
		assertTrue(new Temperature(new Integer(3)).toCInteger() == 3);

		// conversion
		assertTrue(new Temperature(0).toC() == 0);
		assertTrue(new Temperature(0).toF() == 32);
		assertTrue(new Temperature(100).toF() == 212);
	}
}
