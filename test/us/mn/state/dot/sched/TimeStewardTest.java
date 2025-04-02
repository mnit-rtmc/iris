/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.sched;

import junit.framework.TestCase;

/** 
 * Time steward tests
 *
 * @author Doug Lau
 */
public class TimeStewardTest extends TestCase {

	public TimeStewardTest(String name) {
		super(name);
	}

	public void test8601() {
		long ms = 1743609705000L;
		assertTrue(TimeSteward.format8601(ms).equals(
			"2025-04-02T11:01:45-0500"));
		assertTrue(TimeSteward.parse8601("2025-04-02T11:01:45-05")
			== ms);
		assertTrue(TimeSteward.parse8601("2025-04-02T11:01:45-0500")
			== ms);
		assertTrue(TimeSteward.parse8601("2025-04-02T11:01:45-05:00")
			== ms);
	}
}
