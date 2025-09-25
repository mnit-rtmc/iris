/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023-2025  Minnesota Department of Transportation
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

import java.util.Arrays;
import junit.framework.TestCase;

public class SignMsgSourceTest extends TestCase {

	public SignMsgSourceTest(String name) {
		super(name);
	}

	public void testToBits() {
		int bits = SignMsgSource.toBits(SignMsgSource.blank,
			SignMsgSource.external);
		assertTrue("blank+external".equals(
			SignMsgSource.toString(bits)));
		bits = SignMsgSource.toBits(SignMsgSource.operator,
			SignMsgSource.schedule, SignMsgSource.tolling);
		assertTrue("operator+schedule+tolling".equals(
			SignMsgSource.toString(bits)));
	}

	public void testFromString() {
		int bits = SignMsgSource.toBits(SignMsgSource.unknown,
			SignMsgSource.reset, SignMsgSource.blank);
		assertTrue(bits == SignMsgSource.fromString(
			"unknown+reset+blank"));
		bits = SignMsgSource.toBits(SignMsgSource.schedule,
			SignMsgSource.clearguide, SignMsgSource.travel_time);
		assertTrue(bits == SignMsgSource.fromString(
			"schedule+clearguide+travel_time"));
	}
}
