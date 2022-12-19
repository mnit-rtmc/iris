/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import junit.framework.TestCase;
import us.mn.state.dot.tms.server.BoundedSampleHistory;

/** 
 * Bounded sample history test cases
 * @author Doug Lau
 */
public class BoundedSampleHistoryTest extends TestCase {

	/** constructor */
	public BoundedSampleHistoryTest(String name) {
		super(name);
	}

	/** test cases */
	public void test() {
		BoundedSampleHistory hist = new BoundedSampleHistory(4);
		assertTrue(hist.size() == 0);
		assertTrue(null == hist.average(0, 1));
		assertTrue(null == hist.average(0, 2));
		hist.push(10.0);
		assertTrue(hist.size() == 1);
		assertTrue(Double.valueOf(10).equals(hist.get(0)));
		assertTrue(Double.valueOf(10).equals(hist.average(0, 1)));
		assertTrue(Double.valueOf(10).equals(hist.average(0, 2)));
		hist.push(20.0);
		assertTrue(hist.size() == 2);
		assertTrue(Double.valueOf(20).equals(hist.get(0)));
		assertTrue(Double.valueOf(20).equals(hist.average(0, 1)));
		assertTrue(Double.valueOf(15).equals(hist.average(0, 2)));
		hist.clear();
		assertTrue(hist.size() == 0);
		assertTrue(null == hist.average(0, 1));
		hist.push(10.0);
		assertTrue(hist.size() == 1);
		assertTrue(Double.valueOf(10).equals(hist.get(0)));
		assertTrue(Double.valueOf(10).equals(hist.average(0, 1)));
		assertTrue(Double.valueOf(10).equals(hist.average(0, 2)));
		hist.push(20.0);
		assertTrue(hist.size() == 2);
		assertTrue(Double.valueOf(20).equals(hist.get(0)));
		assertTrue(Double.valueOf(20).equals(hist.average(0, 1)));
		assertTrue(Double.valueOf(15).equals(hist.average(0, 2)));
		hist.push(30.0);
		assertTrue(hist.size() == 3);
		assertTrue(Double.valueOf(30).equals(hist.get(0)));
		assertTrue(Double.valueOf(30).equals(hist.average(0, 1)));
		assertTrue(Double.valueOf(25).equals(hist.average(0, 2)));
		assertTrue(Double.valueOf(20).equals(hist.average(0, 3)));
		hist.push(40.0);
		assertTrue(hist.size() == 4);
		assertTrue(Double.valueOf(40).equals(hist.get(0)));
		assertTrue(Double.valueOf(40).equals(hist.average(0, 1)));
		assertTrue(Double.valueOf(35).equals(hist.average(0, 2)));
		assertTrue(Double.valueOf(30).equals(hist.average(0, 3)));
		assertTrue(Double.valueOf(25).equals(hist.average(0, 4)));
		assertTrue(Double.valueOf(20).equals(hist.average(2, 1)));
		assertTrue(Double.valueOf(15).equals(hist.average(2, 2)));
		hist.push(50.0);
		assertTrue(hist.size() == 4);
		assertTrue(Double.valueOf(50).equals(hist.get(0)));
		assertTrue(Double.valueOf(50).equals(hist.average(0, 1)));
		assertTrue(Double.valueOf(45).equals(hist.average(0, 2)));
		assertTrue(Double.valueOf(40).equals(hist.average(0, 3)));
		assertTrue(Double.valueOf(35).equals(hist.average(0, 4)));
		assertTrue(Double.valueOf(30).equals(hist.average(2, 1)));
		assertTrue(Double.valueOf(25).equals(hist.average(2, 2)));
	}

	public void testMissing() {
		BoundedSampleHistory hist = new BoundedSampleHistory(4);
		assertTrue(hist.size() == 0);
		hist.push(10.0);
		hist.push(20.0);
		hist.push(10.0);
		hist.push(20.0);
		assertTrue(hist.size() == 4);
		assertTrue(Double.valueOf(15).equals(hist.average(0, 2)));
		hist.push(null);
		hist.push(null);
		hist.push(null);
		hist.push(null);
		assertTrue(hist.size() == 4);
		assertTrue(hist.average() == null);
	}
}
