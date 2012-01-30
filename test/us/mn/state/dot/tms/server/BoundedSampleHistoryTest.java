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
		BoundedSampleHistory hist = new BoundedSampleHistory(2);
		assertTrue(new Double(0).equals(hist.getAverage(0, 1)));
		assertTrue(new Double(0).equals(hist.getAverage(0, 2)));
		hist.push(10D);
		assertTrue(new Double(10).equals(hist.getAverage(0, 1)));
		assertTrue(new Double(10).equals(hist.getAverage(0, 2)));
		hist.push(20D);
		assertTrue(new Double(20).equals(hist.getAverage(0, 1)));
		assertTrue(new Double(15).equals(hist.getAverage(0, 2)));
		hist = new BoundedSampleHistory(4);
		assertTrue(new Double(0).equals(hist.getAverage(0, 1)));
		hist.push(10D);
		assertTrue(new Double(10).equals(hist.getAverage(0, 1)));
		assertTrue(new Double(10).equals(hist.getAverage(0, 2)));
		hist.push(20D);
		assertTrue(new Double(20).equals(hist.getAverage(0, 1)));
		assertTrue(new Double(15).equals(hist.getAverage(0, 2)));
		hist.push(30D);
		assertTrue(new Double(30).equals(hist.getAverage(0, 1)));
		assertTrue(new Double(25).equals(hist.getAverage(0, 2)));
		assertTrue(new Double(20).equals(hist.getAverage(0, 3)));
		hist.push(40D);
		assertTrue(new Double(40).equals(hist.getAverage(0, 1)));
		assertTrue(new Double(35).equals(hist.getAverage(0, 2)));
		assertTrue(new Double(30).equals(hist.getAverage(0, 3)));
		assertTrue(new Double(25).equals(hist.getAverage(0, 4)));
		assertTrue(new Double(20).equals(hist.getAverage(2, 1)));
		assertTrue(new Double(15).equals(hist.getAverage(2, 2)));
	}
}
