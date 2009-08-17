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
package us.mn.state.dot.tms;

import junit.framework.TestCase;
import us.mn.state.dot.tms.DmsPgTime;

/** 
 * DmsPgTime test cases
 * @author Michael Darter
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

		// validateOnTime
		assertTrue(DmsPgTime.validateOnTime(
			new DmsPgTime(0), true).toTenths() 
			== DmsPgTime.getDefaultOn(true).toTenths());
		assertTrue(DmsPgTime.validateOnTime(
			new DmsPgTime(0), false).toTenths() 
			== DmsPgTime.MIN_ONTIME.toTenths());

		// validateValue: single page
		assertTrue(new DmsPgTime(0).equals(
			new DmsPgTime(-3).validateValue(true, 
			new DmsPgTime(.5), new DmsPgTime(10.0))));
		assertTrue(new DmsPgTime(0).equals(
			new DmsPgTime(-3).validateValue(true, 
			new DmsPgTime(.5), new DmsPgTime(10.0))));
		assertTrue(new DmsPgTime(0).equals(
			new DmsPgTime(0).validateValue(true, 
			new DmsPgTime(.5), new DmsPgTime(10.0))));
		assertTrue(new DmsPgTime(0).equals(
			new DmsPgTime(.4).validateValue(true, 
			new DmsPgTime(.5), new DmsPgTime(10.0))));
		assertTrue(new DmsPgTime(26).equals(
			new DmsPgTime(2.6).validateValue(true, 
			new DmsPgTime(.5), new DmsPgTime(10.0))));
		assertTrue(new DmsPgTime(100).equals(
			new DmsPgTime(12.0).validateValue(true, 
			new DmsPgTime(.5), new DmsPgTime(10.0))));

		// validateValue: multi page
		assertTrue(new DmsPgTime(5).equals(
			new DmsPgTime(-3.3).validateValue(false, 
			new DmsPgTime(.5), new DmsPgTime(10.0))));
		assertTrue(new DmsPgTime(5).equals(
			new DmsPgTime(0).validateValue(false, 
			new DmsPgTime(.5), new DmsPgTime(10.0))));
		assertTrue(new DmsPgTime(5).equals(
			new DmsPgTime(.4).validateValue(false, 
			new DmsPgTime(.5), new DmsPgTime(10.0))));
		assertTrue(new DmsPgTime(26).equals(
			new DmsPgTime(2.6).validateValue(false, 
			new DmsPgTime(.5), new DmsPgTime(10.0))));
		assertTrue(new DmsPgTime(100).equals(
			new DmsPgTime(12.0).validateValue(false, 
			new DmsPgTime(.5), new DmsPgTime(10.0))));
	}
}
