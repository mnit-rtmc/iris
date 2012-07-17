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
 * Length test cases
 *
 * @author Michael Darter
 */
public class LengthTest extends TestCase {

	/** constructor */
	public LengthTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {

		// missing
		assertTrue(new Length().isMissing());
		assertTrue(new Length().toM() == 0d);
		assertFalse(new Length(0).isMissing());

		// to string
		assertTrue(new Length().toString().equals(
			PhysicalQuantity.MISSING));
		assertTrue(new Length().toString2() == null);
		assertTrue(new Length(5).toString().equals("5 m"));

		// conversion
		assertTrue(new Length(0).toM() == 0);
		assertTrue(new Length(0).toFt() == 0);
		assertTrue(new Length(100).toFt() == 328.08399000000003);
	}
}
