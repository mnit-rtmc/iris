/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
 * Speed tests
 *
 * @author Douglas Lau
 */
public class SpeedTest extends TestCase {

	public SpeedTest(String name) {
		super(name);
	}

	public void test() {
		Speed.Formatter df = new Speed.Formatter(2);
		assertTrue(df.format(new Speed(1)).equals("1.00 kph"));
		assertTrue(df.format(new Speed(5)).equals("5.00 kph"));
		assertTrue(df.format(new Speed(1).convert(
			Speed.Units.MPH)).equals("0.62 mph"));
		assertTrue(df.format(new Speed(100, Speed.Units.KPH).convert(
			Speed.Units.MPH)).equals("62.14 mph"));
		assertTrue(df.format(new Speed(60, Speed.Units.MPH).convert(
			Speed.Units.KPH)).equals("96.56 kph"));
		assertTrue(df.format(new Speed(60, Speed.Units.MPH).convert(
			Speed.Units.FPS)).equals("88.00 fps"));
		assertTrue(df.format(new Speed(88, Speed.Units.FPS).convert(
			Speed.Units.MPH)).equals("60.00 mph"));
	}

	public void test2() {
		Distance d;
		Interval i;
		Speed.Formatter df = new Speed.Formatter(2);
		d = new Distance(60, Distance.Units.MILES);
		i = new Interval(1, Interval.Units.HOURS);
		assertTrue(df.format(new Speed(d, i).convert(
			Speed.Units.MPH)).equals("60.00 mph"));
		d = new Distance(60, Distance.Units.MILES);
		i = new Interval(2, Interval.Units.HOURS);
		assertTrue(df.format(new Speed(d, i).convert(
			Speed.Units.MPH)).equals("30.00 mph"));
		d = new Distance(88, Distance.Units.FEET);
		i = new Interval(1, Interval.Units.SECONDS);
		assertTrue(df.format(new Speed(d, i).convert(
			Speed.Units.FPS)).equals("88.00 fps"));
		d = new Distance(316800, Distance.Units.FEET);
		i = new Interval(1, Interval.Units.HOURS);
		assertTrue(df.format(new Speed(d, i).convert(
			Speed.Units.MPH)).equals("60.00 mph"));
		d = new Distance(0.01, Distance.Units.KILOMETERS);
		i = new Interval(1, Interval.Units.SECONDS);
		assertTrue(df.format(new Speed(d, i).convert(
			Speed.Units.KPH)).equals("36.00 kph"));
	}

	public void test3() {
		Interval.Formatter f = new Interval.Formatter(2);
		assertTrue(f.format(new Speed(60, Speed.Units.MPH).elapsed(
			new Distance(60, Distance.Units.MILES))).equals(
			"3,600.00 s"));
		assertTrue(f.format(new Speed(60, Speed.Units.MPH).elapsed(
			new Distance(1, Distance.Units.MILES))).equals(
			"60.00 s"));
		assertTrue(f.format(new Speed(88, Speed.Units.FPS).elapsed(
			new Distance(1, Distance.Units.MILES))).equals(
			"60.00 s"));
		assertTrue(f.format(new Speed(88, Speed.Units.FPS).elapsed(
			new Distance(88, Distance.Units.FEET))).equals(
			"1.00 s"));
	}
}
