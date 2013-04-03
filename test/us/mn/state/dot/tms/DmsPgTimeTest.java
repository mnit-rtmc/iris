/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DmsPgTime;
import us.mn.state.dot.tms.units.Interval;

/**
 * DmsPgTime test cases
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class DmsPgTimeTest extends TestCase {

	/** constructor */
	public DmsPgTimeTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {
		// equals
		assertTrue(new DmsPgTime(13).equals(new DmsPgTime(13)));
		assertFalse(new DmsPgTime(12).equals(new DmsPgTime(13)));
		assertTrue(new DmsPgTime(10.3).equals(new DmsPgTime(103)));
		assertTrue(new DmsPgTime(10.3f).equals(new DmsPgTime(10.3d)));
		assertFalse(new DmsPgTime(33).equals(new DmsPgTime(34)));

		// isZero
		assertTrue(new DmsPgTime(0).isZero());
		assertFalse(new DmsPgTime(1).isZero());

		// validateOnInterval
		assertTrue(DmsPgTime.validateOnInterval(new Interval(0), true).
			equals(DmsPgTime.defaultPageOnInterval(true)));
		// a validated multipage on-time should equal the minimum
		assertTrue(DmsPgTime.validateOnInterval(new Interval(0), false).
			equals(DmsPgTime.minPageOnInterval()));

		// validateValue: single page
		assertTrue(new Interval(0).equals(
			DmsPgTime.validateValue(new Interval(-3), true,
			new Interval(.5), new Interval(10.0))));
		assertTrue(new Interval(0).equals(
			DmsPgTime.validateValue(new Interval(-3), true,
			new Interval(.5), new Interval(10.0))));
		assertTrue(new Interval(0).equals(
			DmsPgTime.validateValue(new Interval(0), true,
			new Interval(.5), new Interval(10.0))));
		assertTrue(new Interval(0).equals(
			DmsPgTime.validateValue(new Interval(.4), true,
			new Interval(.5), new Interval(10.0))));
		assertTrue(new Interval(2.6).equals(
			DmsPgTime.validateValue(new Interval(2.6), true,
			new Interval(.5), new Interval(10.0))));
		assertTrue(new Interval(10.0).equals(
			DmsPgTime.validateValue(new Interval(12.0), true,
			new Interval(.5), new Interval(10.0))));

		// validateValue: multi page
		assertTrue(new Interval(.5).equals(
			DmsPgTime.validateValue(new Interval(-3.3), false,
			new Interval(.5), new Interval(10.0))));
		assertTrue(new Interval(.5).equals(
			DmsPgTime.validateValue(new Interval(0), false,
			new Interval(.5), new Interval(10.0))));
		assertTrue(new Interval(.5).equals(
			DmsPgTime.validateValue(new Interval(.4), false,
			new Interval(.5), new Interval(10.0))));
		assertTrue(new Interval(2.6).equals(
			DmsPgTime.validateValue(new Interval(2.6), false,
			new Interval(.5), new Interval(10.0))));
		assertTrue(new Interval(10.0).equals(
			DmsPgTime.validateValue(new Interval(12.0), false,
			new Interval(.5), new Interval(10.0))));
	}
}
