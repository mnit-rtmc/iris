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
package us.mn.state.dot.tms.client.dms;

import junit.framework.TestCase;

/** 
 * PgTimeSpinner test cases
 * @author Michael Darter
 */
public class PgTimeSpinnerTest extends TestCase {

	/** constructor */
	public PgTimeSpinnerTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {
		// roundSingle
		assertTrue(PgTimeSpinner.roundSingle(.6) == .6);
		assertTrue(PgTimeSpinner.roundSingle(.59) == .6);

		// validateValue: single page
		assertTrue(0.0 == PgTimeSpinner.validateValue(-3, true, .5, 10.0));
		assertTrue(0.0 == PgTimeSpinner.validateValue(0, true, .5, 10.0));
		assertTrue(0.0 == PgTimeSpinner.validateValue(.4, true, .5, 10.0));
		assertTrue(2.6 == PgTimeSpinner.validateValue(2.6, true, .5, 10.0));
		assertTrue(10.0 == PgTimeSpinner.validateValue(12, true, .5, 10.0));

		// validateValue: multi page
		assertTrue(0.5 == PgTimeSpinner.validateValue(-3.3, false, .5, 10.0));
		assertTrue(0.5 == PgTimeSpinner.validateValue(0, false, .5, 10.0));
		assertTrue(0.5 == PgTimeSpinner.validateValue(.4, false, .5, 10.0));
		assertTrue(2.6 == PgTimeSpinner.validateValue(2.6, false, .5, 10.0));
		assertTrue(10.0 == PgTimeSpinner.validateValue(12, false, .5, 10.0));
	}
}
