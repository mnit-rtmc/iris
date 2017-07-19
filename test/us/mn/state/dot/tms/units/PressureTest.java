/*
 * IRIS -- Intelligent Roadway Information System
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
 * Pressure tests
 *
 * @author Michael Darter
 */
public class PressureTest extends TestCase {

	public PressureTest(String name) {
		super(name);
	}

	public void test() {

		// constructors
		assertTrue(new Pressure(1).toString().equals("1 Pa"));
		assertTrue(new Pressure(1000).toString().equals("1,000 Pa"));
		assertTrue(new Pressure(1, Pressure.Units.PASCALS).
			toString().equals("1 Pa"));
		assertTrue(new Pressure(1, Pressure.Units.HECTOPASCALS).
			toString().equals("1 hPa"));
		assertTrue(new Pressure(1, Pressure.Units.INHG).
			toString().equals("1.0 inHg"));

		// Pa and hPa
		assertTrue(new Pressure(77370, Pressure.Units.PASCALS).
			convert(Pressure.Units.HECTOPASCALS).
			toString().equals("774 hPa"));
		assertTrue(new Pressure(773.7, Pressure.Units.HECTOPASCALS).
			convert(Pressure.Units.PASCALS).
			toString().equals("77,370 Pa"));

		// Pa and inHg
		assertTrue(new Pressure(77370, Pressure.Units.PASCALS).
			convert(Pressure.Units.INHG).
			toString().equals("22.8 inHg"));
		assertTrue(new Pressure(22.8, Pressure.Units.INHG).
			convert(Pressure.Units.PASCALS).
			toString().equals("77,210 Pa"));
	}
}
