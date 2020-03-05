/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2019  Minnesota Department of Transportation
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

import java.util.Calendar;
import java.util.Iterator;
import junit.framework.TestCase;

/** 
 * Periodic Sample Cache test cases
 * @author Doug Lau
 */
public class PeriodicSampleCacheTest extends TestCase {

	static private final long[] T = new long[12];
	static {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		for (int i = 0; i < 12; i++) {
			T[i] = cal.getTimeInMillis();
			cal.add(Calendar.SECOND, 30);
		}
	}

	public PeriodicSampleCacheTest(String name) {
		super(name);
	}

	public void testVehCount() {
		PeriodicSampleCache cache = new PeriodicSampleCache(
			PeriodicSampleType.VEH_COUNT);
		assertTrue(isEmpty(cache));
		cache.add(new PeriodicSample(T[1], 30, 2), "test");
		assertFalse(isEmpty(cache));
		assertTrue(cache.getValue(T[0], T[1]) == 2);
		assertTrue(cache.getValue(T[1], T[2]) == -1);
		assertTrue(cache.getValue(T[0], T[2]) == 4);
		assertTrue(cache.getValue(T[0], T[3]) == -1);
		cache.add(new PeriodicSample(T[2], 60, 4), "test");
		assertFalse(isEmpty(cache));
		assertTrue(cache.getValue(T[0], T[1]) == 2);
		assertTrue(cache.getValue(T[1], T[2]) == 2);
		assertTrue(cache.getValue(T[0], T[2]) == 4);
		assertTrue(cache.getValue(T[0], T[3]) == 6);
		assertTrue(areSamplesEqual(cache, 2));
	}

	public void testOccupancy() {
		PeriodicSampleCache cache = new PeriodicSampleCache(
			PeriodicSampleType.OCCUPANCY);
		assertTrue(isEmpty(cache));
		cache.add(new PeriodicSample(T[1], 30, 5), "test");
		assertFalse(isEmpty(cache));
		cache.add(new PeriodicSample(T[2], 60, 5), "test");
		assertFalse(isEmpty(cache));
		assertTrue(areSamplesEqual(cache, 5));
	}

/* non-deterministic...
	public void testSpeed() {
		PeriodicSampleCache cache = new PeriodicSampleCache(
			PeriodicSampleType.SPEED);
		assertTrue(isEmpty(cache));
		cache.add(new PeriodicSample(T[1], 30, 10), "test");
		cache.add(new PeriodicSample(T[2], 30, 15), "test");
		cache.add(new PeriodicSample(T[3], 30, 20), "test");
		cache.add(new PeriodicSample(T[4], 30, 25), "test");
		// missing sample T[5]
		// missing sample T[6]
		cache.add(new PeriodicSample(T[7], 30, 35), "test");
		cache.add(new PeriodicSample(T[8], 30, 40), "test");
		cache.add(new PeriodicSample(T[9], 30, 45), "test");
		cache.add(new PeriodicSample(T[10], 30, 50), "test");
		cache.add(new PeriodicSample(T[11], 300, 30), "test");
		assertFalse(isEmpty(cache));
		Iterator<PeriodicSample> it = cache.iterator();
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 10);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 15);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 20);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 25);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 30);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 30);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 35);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 40);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 45);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 50);
		assertFalse(it.hasNext());
	}
*/

	public void testScan() {
		PeriodicSampleCache cache = new PeriodicSampleCache(
			PeriodicSampleType.SCAN);
		assertTrue(isEmpty(cache));
		cache.add(new PeriodicSample(T[1], 30, 100), "test");
		cache.add(new PeriodicSample(T[2], 30, 150), "test");
		cache.add(new PeriodicSample(T[3], 30, 200), "test");
		cache.add(new PeriodicSample(T[4], 30, 250), "test");
		cache.add(new PeriodicSample(T[5], 30, 300), "test");
		cache.add(new PeriodicSample(T[6], 30, 350), "test");
		cache.add(new PeriodicSample(T[7], 30, 400), "test");
		cache.add(new PeriodicSample(T[8], 30, 450), "test");
		cache.add(new PeriodicSample(T[9], 30, 500), "test");
		cache.add(new PeriodicSample(T[10], 30, 550), "test");
		assertFalse(isEmpty(cache));
		cache.purge(T[6]);
		Iterator<PeriodicSample> it = cache.iterator();
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 350);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 400);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 450);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 500);
		assertTrue(it.hasNext());
		assertTrue(it.next().value == 550);
		assertFalse(it.hasNext());
	}

	private boolean isEmpty(PeriodicSampleCache cache) {
		return !cache.iterator().hasNext();
	}

	private boolean areSamplesEqual(PeriodicSampleCache cache, int val) {
		Iterator<PeriodicSample> it = cache.iterator();
		while (it.hasNext()) {
			PeriodicSample ps = it.next();
			if (ps.value != val)
				return false;
		}
		return true;
	}
}
