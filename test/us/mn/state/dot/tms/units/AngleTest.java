/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  AHMCT, University of California
 * Copyright (C) 2017  Iteris Inc.
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
 * Angle test cases
 *
 * @author Michael Darter
 */
public class AngleTest extends TestCase {

	/** constructor */
	public AngleTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {
		final double REV = 2 * Math.PI;

		// missing
		assertTrue(new Angle().toRads() == 0d);
		assertTrue(new Angle().toDegs() == 0);

		// get degrees Integer
		assertTrue(new Angle().toDegsInt() == 0);
		assertTrue(new Angle(180).toDegsInt() == 180);
		assertTrue(new Angle(500).toDegsInt() == 500);
		assertTrue(new Angle(-500).toDegsInt() == -500);

		// get rads
		assertTrue(new Angle(180).toRads() == Math.PI);
		assertTrue(new Angle(360 * 5).toRads() == 5 * REV);

		// to string
		assertTrue(new Angle(5).toString().equals("5\u00B0"));

		// get short direction
		assertTrue(new Angle(0).toShortDir().equals("N"));
		assertTrue(new Angle(-445).toShortDir().equals("W"));
		assertTrue(new Angle(-10).toShortDir().equals("N"));
		assertTrue(new Angle(0).toShortDir().equals("N"));
		assertTrue(new Angle(90).toShortDir().equals("E"));
		assertTrue(new Angle(180).toShortDir().equals("S"));
		assertTrue(new Angle(270).toShortDir().equals("W"));
		assertTrue(new Angle(315).toShortDir().equals("NW"));
		assertTrue(new Angle(445).toShortDir().equals("E"));

		// normalize degrees
		assertTrue(new Angle(0).toNormalizedDegs() == 0);
		assertTrue(new Angle(-1).toNormalizedDegs() == 359);
		assertTrue(new Angle(-10).toNormalizedDegs() == 350);
		assertTrue(new Angle(-180).toNormalizedDegs() == 180);
		assertTrue(new Angle(-540).toNormalizedDegs() == 180);
		assertTrue(new Angle(180).toNormalizedDegs() == 180);
		assertTrue(new Angle(360).toNormalizedDegs() == 0);
		assertTrue(new Angle(540).toNormalizedDegs() == 180);

		// round
		assertTrue(Angle.round(33.33) == 33);

		// round
		assertTrue(new Angle(2).round(10).toDegs() == 0);
		assertTrue(new Angle(8).round(10).toDegs() == 10);
		assertTrue(new Angle(350).round(10).toDegsInt() == 350);
		assertTrue(new Angle(356).round(10).toDegs() == 360);
		assertTrue(new Angle(176).round(10).equals(
			new Angle(182).round(10)));

		// equals
		assertTrue(new Angle().equals(new Angle()));
		assertTrue(new Angle().equals(new Angle(0)));
		assertFalse(new Angle().equals(new Angle(180)));
		assertTrue(new Angle(180).equals(new Angle(180)));

		// ceilRev
		assertTrue(Angle.ceilRev(.25 * REV) == 1 * REV);
		assertTrue(Angle.ceilRev(1.1 * REV) == 2 * REV);
		assertTrue(Angle.ceilRev(2.5 * REV) == 3 * REV);
		assertTrue(Angle.ceilRev(-.25 * REV) == -1 * REV);
		assertTrue(Angle.ceilRev(-1.1 * REV) == -2 * REV);
		assertTrue(Angle.ceilRev(-2.5 * REV) == -3 * REV);

		// floorRev
		assertTrue(Angle.floorRev(.25 * REV) == 0 * REV);
		assertTrue(Angle.floorRev(1.1 * REV) == 1 * REV);
		assertTrue(Angle.floorRev(2.5 * REV) == 2 * REV);
		assertTrue(Angle.floorRev(-.25 * REV) == 0 * REV);
		assertTrue(Angle.floorRev(-1.1 * REV) == -1 * REV);
		assertTrue(Angle.floorRev(-2.5 * REV) == -2 * REV);

		// invert
		assertTrue(new Angle().invert().toDegs() == 0);
		assertTrue(new Angle(0).invert().toDegs() == 0);
		assertTrue(new Angle(10).invert().toDegsInt() == 350);
		assertTrue(new Angle(90).invert().toDegs() == 270);
		assertTrue(new Angle(180).invert().toDegs() == 180);
		assertTrue(new Angle(270).invert().toDegs() == 90);
		assertTrue(new Angle(359).invert().toDegsInt() == 1);
		assertTrue(new Angle(360 + 10).invert().
			toDegs() == 360 + 350);
		assertTrue(new Angle(360 + 180 - 10).invert().
			toDegs() == 360 + 180 + 10);
		assertTrue(new Angle(360 + 180).invert().
			toDegs() == 360 + 180);
		assertTrue(new Angle(-10).invert().toDegsInt() == -350);
		assertTrue(new Angle(-90).invert().toDegsInt() == -270);
		assertTrue(new Angle(-360 - 10).invert().
			toDegsInt() == -360 - 350);

		// add: this tests conversion from NTCIP 1204 angles
		//      to Java transform angles + 90 for marker
		assertTrue(new Angle(180).add(-0).toDegs() == 180);
		assertTrue(new Angle(180).add(-45).toDegs() == 135);
		assertTrue(new Angle(180).add(-90).toDegs() == 90);
		assertTrue(new Angle(180).add(-135).toDegs() == 45);
		assertTrue(new Angle(180).add(-180).toDegs() == 0);
		assertTrue(new Angle(180).add(-225).toDegs() == -45);
		assertTrue(new Angle(180).add(-270).toDegs() == -90);
	}
}
