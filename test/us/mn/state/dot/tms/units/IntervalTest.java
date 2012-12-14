/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
 * Interval tests
 *
 * @author Douglas Lau
 */
public class IntervalTest extends TestCase {

	public IntervalTest(String name) {
		super(name);
	}

	public void test() {
		Interval.Formatter f = new Interval.Formatter(0);
		assertTrue(f.format(new Interval(1)).equals("1 s"));
		assertTrue(f.format(new Interval(5)).equals("5 s"));
		assertTrue(f.format(new Interval(1).convert(
			Interval.Units.MILLISECONDS)).equals("1,000 ms"));
		assertTrue(f.format(new Interval(1, Interval.Units.HOURS).
			convert(Interval.Units.SECONDS)).equals("3,600 s"));
		assertTrue(f.format(new Interval(1, Interval.Units.DAYS).
			convert(Interval.Units.HOURS)).equals("24 hr"));
		assertTrue(f.format(new Interval(1, Interval.Units.WEEKS).
			convert(Interval.Units.HOURS)).equals("168 hr"));

		assertTrue(new Interval(1, Interval.Units.MINUTES).ms() ==
			60000);
		assertTrue(new Interval(5, Interval.Units.MINUTES).seconds() ==
			300);
		assertTrue(new Interval(499, Interval.Units.MILLISECONDS).round(
			Interval.Units.SECONDS) == 0);
		assertTrue(new Interval(500, Interval.Units.MILLISECONDS).round(
			Interval.Units.SECONDS) == 1);
		assertTrue(new Interval(30, Interval.Units.SECONDS).per(
			Interval.HOUR) == 120);
		assertTrue(Interval.HOUR.divide(30) == 120);
		assertTrue(new Interval(60).equals(new Interval(1,
			Interval.Units.MINUTES)));
	}
}
