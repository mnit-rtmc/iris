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
import static us.mn.state.dot.tms.units.Temperature.Units.CELSIUS;
import static us.mn.state.dot.tms.units.Temperature.Units.FAHRENHEIT;

/**
 * Temperature tests
 *
 * @author Douglas Lau
 */
public class TemperatureTest extends TestCase {

	public TemperatureTest(String name) {
		super(name);
	}

	public void test1() {
		Temperature.Formatter tf = new Temperature.Formatter(2);
		assertTrue(tf.format(new Temperature(0)).equals(
			"0.00 \u00B0C"));
		assertTrue(tf.format(new Temperature(100)).equals(
			"100.00 \u00B0C"));
	}
	public void test2() {
		Temperature.Formatter tf = new Temperature.Formatter(2);
		assertTrue(tf.format(new Temperature(-40, CELSIUS).convert(
			FAHRENHEIT)).equals("-40.00 \u00B0F"));
		assertTrue(tf.format(new Temperature(0, CELSIUS).convert(
			FAHRENHEIT)).equals("32.00 \u00B0F"));
		assertTrue(tf.format(new Temperature(37, CELSIUS).convert(
			FAHRENHEIT)).equals("98.60 \u00B0F"));
		assertTrue(tf.format(new Temperature(100, CELSIUS).convert(
			FAHRENHEIT)).equals("212.00 \u00B0F"));
		assertTrue(tf.format(new Temperature(-40, FAHRENHEIT).convert(
			CELSIUS)).equals("-40.00 \u00B0C"));
		assertTrue(tf.format(new Temperature(32, FAHRENHEIT).convert(
			CELSIUS)).equals("0.00 \u00B0C"));
		assertTrue(tf.format(new Temperature(98.6, FAHRENHEIT).convert(
			CELSIUS)).equals("37.00 \u00B0C"));
		assertTrue(tf.format(new Temperature(212, FAHRENHEIT).convert(
			CELSIUS)).equals("100.00 \u00B0C"));
	}
	public void test3() {
		assertTrue(-40 == new Temperature(-40, CELSIUS).round(
			FAHRENHEIT));
		assertTrue(32 == new Temperature(0, CELSIUS).round(FAHRENHEIT));
		assertTrue(99 ==new Temperature(37, CELSIUS).round(FAHRENHEIT));
		assertTrue(212 == new Temperature(100, CELSIUS).round(
			FAHRENHEIT));
		assertTrue(-40 == new Temperature(-40, FAHRENHEIT).round(
			CELSIUS));
		assertTrue(0 == new Temperature(32, FAHRENHEIT).round(CELSIUS));
		assertTrue(37 == new Temperature(98.6, FAHRENHEIT).round(
			CELSIUS));
		assertTrue(100 == new Temperature(212, FAHRENHEIT).round(
			CELSIUS));
	}
}
